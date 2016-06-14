package mypkg;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class MyService {

    private final EntityManager em;

    @Inject
    public MyService(final EntityManager em) {
        this.em = em;
    }

    public String getMycol() {
        return em.createQuery("select e from MyTable e", MyTable.class).getSingleResult().getMycol();
    }
}
