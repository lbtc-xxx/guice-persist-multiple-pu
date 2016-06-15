package nestedservice;

import com.google.inject.persist.Transactional;

import javax.inject.Inject;

public class TransactionalBazService {

    @Inject
    FooService fooService;
    @Inject
    BarService barService;

    public String findViaFoo() {
        return fooService.find();
    }

    public String findViaBar() {
        return barService.find();
    }

    @Transactional
    public void save() {
        fooService.save();
        barService.save();
    }
}
