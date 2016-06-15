package nestedservice;

import mypkg.MasterDatabase;

import javax.inject.Inject;

public class MasterBazService {

    @Inject
    @MasterDatabase
    FooService fooService;
    @Inject
    @MasterDatabase
    BarService barService;

    public String findViaFoo() {
        return fooService.find();
    }

    public String findViaBar() {
        return barService.find();
    }
}
