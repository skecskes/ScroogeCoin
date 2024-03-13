import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CryptoTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void run() {
        Chain.main(new String[] { "SimpleCoin" });
    }
}
