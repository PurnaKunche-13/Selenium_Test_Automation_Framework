import com.purna.automation.utilities.ConfigReader;
import com.purna.automation.utilities.RetryBudget;
import com.purna.automation.utilities.SafeDiagnostics;
import java.util.*;
import java.util.concurrent.*;
public class ComponentChecks {
    private static int checks;
    private static void equal(Object actual,Object expected) {
        if (!actual.equals(expected)) throw new AssertionError(actual+" != "+expected);
        checks++;
    }
    public static void main(String[] args) throws Exception {
        if(args.length>0 && args[0].equals("environment")) {
            equal(ConfigReader.environment(),"qa");
            equal(ConfigReader.positiveInt("explicitWait"),29);
            System.setProperty("explicitWait","11");
            equal(ConfigReader.positiveInt("explicitWait"),11);
            System.out.println("Environment precedence probe: 3 assertions passed.");
            return;
        }
        ProfileCheck();BudgetCheck();SafetyCheck();
    }
    private static void ProfileCheck() {
        checks=0;
        equal(ConfigReader.environment(),"local");
        equal(ConfigReader.positiveInt("explicitWait"),15);
        System.setProperty("env","qa");
        equal(ConfigReader.positiveInt("explicitWait"),20);
        equal(ConfigReader.get("apiBaseUrl"),"https://jsonplaceholder.typicode.com");
        System.setProperty("env","stage");
        equal(ConfigReader.positiveInt("explicitWait"),25);
        System.setProperty("explicitWait","9");
        equal(ConfigReader.positiveInt("explicitWait"),9);
        System.clearProperty("explicitWait");
        equal(ConfigReader.nonNegativeInt("maxRetries"),1);
        System.setProperty("env","production");
        try { ConfigReader.get("browser"); throw new AssertionError("Unknown profile accepted"); }
        catch(IllegalArgumentException expected) {checks++;}
        System.clearProperty("env");
        System.out.println("Feature 1: compiled ConfigReader; "+checks+" profile/precedence assertions passed.");
    }

    private static void BudgetCheck() throws Exception {
        RetryBudget budget=new RetryBudget();
        int checks=0;
        if(budget.reserve("zero",0))throw new AssertionError(); checks++;
        if(!budget.reserve("a",1)||budget.reserve("a",1))throw new AssertionError(); checks++;
        if(!budget.reserve("b",1)||budget.used("a")!=1)throw new AssertionError(); checks++;
        budget.finish("a"); if(budget.used("a")!=0)throw new AssertionError(); checks++;
        var pool=Executors.newFixedThreadPool(4);
        try {
            var tasks=new ArrayList<Callable<Boolean>>();
            for(int i=0;i<50;i++)tasks.add(()->budget.reserve("shared",1));
            int accepted=0;for(var f:pool.invokeAll(tasks))if(f.get())accepted++;
            if(accepted!=1)throw new AssertionError("Concurrent budget accepted "+accepted); checks++;
        } finally {pool.shutdownNow();}
        budget.clear();if(budget.used("shared")!=0)throw new AssertionError(); checks++;
        System.out.println("Feature 3: compiled RetryBudget; "+checks+" assertions passed, including 50 concurrent reservations.");
    }

    private static void SafetyCheck() {
        int checks=0;
        String clean=SafeDiagnostics.location("https://user:password@example.test/inventory.html?token=secret#fragment");
        if(!clean.equals("https://example.test/inventory.html"))throw new AssertionError(clean);checks++;
        if(SafeDiagnostics.location("https://example.test/reset/very-secret-token").contains("very-secret"))throw new AssertionError();checks++;
        if(!SafeDiagnostics.location("data:text/html,password").equals("data:[content omitted]"))throw new AssertionError();checks++;
        if(!SafeDiagnostics.location("https://example.test/posts/1?apiKey=secret").equals("https://example.test/posts/1"))throw new AssertionError();checks++;
        if(!SafeDiagnostics.contentType("application/json; charset=UTF-8").equals("application/json"))throw new AssertionError();checks++;
        if(SafeDiagnostics.exception(new IllegalArgumentException("password=secret bearer=token")).contains("password=secret"))throw new AssertionError();checks++;
        System.out.println("Feature 4: compiled SafeDiagnostics; "+checks+" URL/exception/header safety assertions passed.");
    }

}
