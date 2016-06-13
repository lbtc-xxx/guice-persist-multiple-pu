package mypkg;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Singleton
public class SlaveServlet2 extends HttpServlet {

    @Inject
    Provider<MyTableService2> service2;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter writer = resp.getWriter();
        writer.write(service2.get().get());
    }
}
