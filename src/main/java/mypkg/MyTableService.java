package mypkg;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class MyTableService {

    private final EntityManager em;

    @Inject
    public MyTableService(final EntityManager em) {
        this.em = em;
    }

    public String get() {
        return em.createQuery("select e from MyTable e", MyTable.class).getSingleResult().getMycol();
    }
}
