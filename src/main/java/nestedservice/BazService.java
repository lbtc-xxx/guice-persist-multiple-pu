package nestedservice;

import javax.inject.Inject;

public class BazService {

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
}
