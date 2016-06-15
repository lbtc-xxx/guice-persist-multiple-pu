package nestedservice;

import com.google.inject.persist.Transactional;
import mypkg.MasterDatabase;

import javax.inject.Inject;

public class BazService {

    @Inject
    @MasterDatabase
    FooService fooService;
    @Inject
    @MasterDatabase
    BarService barService;

    @Transactional
    public void save() {
        fooService.save();
        barService.save();
    }
}
