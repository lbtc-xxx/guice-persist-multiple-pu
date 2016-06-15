package nestedservice;

import mypkg.MyTable;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class FooService {

    @Inject
    EntityManager em;

    public String find() {
        return em.createNamedQuery("MyTable.findAll", MyTable.class).getResultList().get(0).getMycol();
    }

    public void save() {
        final MyTable myTable = new MyTable();
        myTable.setMycol("foo");
        em.persist(myTable);
    }
}
