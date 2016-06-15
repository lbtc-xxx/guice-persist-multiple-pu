package nestedservice;

import mypkg.SlaveDatabase;

import javax.inject.Inject;

public class SlaveBazService {

    @Inject
    @SlaveDatabase
    FooService fooService;
    @Inject
    @SlaveDatabase
    BarService barService;

    public String findViaFoo() {
        return fooService.find();
    }

    public String findViaBar() {
        return barService.find();
    }
}
