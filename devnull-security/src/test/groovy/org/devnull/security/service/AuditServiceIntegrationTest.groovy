package org.devnull.security.service

import org.devnull.security.BaseSecurityIntegrationTest
import org.devnull.security.dao.AuditedWidgetDao
import org.devnull.security.dao.UserDao
import org.devnull.security.model.AuditedWidget
import org.devnull.security.model.User
import org.hibernate.envers.RevisionType
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.NotTransactional

class AuditServiceIntegrationTest extends BaseSecurityIntegrationTest {
    @Autowired
    AuditedWidgetDao dao

    @Autowired
    UserDao userDao

    @Autowired
    AuditService auditService

    User auditor

    @Before
    void login() {
        auditor = userDao.findOne(1)
        loginAsUser(auditor)
    }

    /**
     * Test must not be transactional in order for listeners to fire.
     */
    @Test
    @NotTransactional
    void shouldCreateRevisions() {
        def widget = new AuditedWidget(name: "another widget")
        dao.save(widget)
        widget.name = "yet another widget"
        dao.save(widget)
        dao.delete(widget.id)

        def audits = auditService.findAllByEntity(AuditedWidget)
        assert audits.size() == 3

        assert audits[0].type == RevisionType.ADD
        assert audits[0].entity.name == "another widget"
        assert audits[0].revision.userName == auditor.userName

        assert audits[1].type == RevisionType.MOD
        assert audits[1].entity.name == "yet another widget"
        assert audits[1].revision.userName == auditor.userName

        assert audits[2].type == RevisionType.DEL
        assert audits[2].entity.name == null
        assert audits[2].revision.userName == auditor.userName
    }


}