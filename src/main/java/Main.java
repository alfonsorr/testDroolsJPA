import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.drools.core.marshalling.impl.ProtobufMessages;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.postgresql.ds.PGSimpleDataSource;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by aroa on 15/02/16.
 */
public class Main {
    public static void main (String[] args) throws NamingException, HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException, NotSupportedException {

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "bitronix.tm.jndi.BitronixInitialContextFactory");




        PoolingDataSource ds = new PoolingDataSource();
        ds.setUniqueName("jdbc/prueba");
        ds.setClassName("org.postgresql.xa.PGXADataSource");
        ds.setMaxPoolSize(3);
        ds.setAllowLocalTransactions(true);
        ds.getDriverProperties().put("databaseName", "prueba");
        ds.getDriverProperties().put("user", "postgres");
        ds.getDriverProperties().put("password", "postgres");
        ds.getDriverProperties().put("serverName", "127.0.0.1");
        ds.getDriverProperties().put("portNumber", "5432");

        ds.init();

        List res = new ArrayList();

        KieServices kieServices = KieServices.Factory.get();
        ReleaseId releaseId = kieServices.newReleaseId( "demo", "PostgresqlProject", "1.0" );
        KieContainer kieContainer = kieServices.newKieContainer( releaseId );

        KieSession sess = kieContainer.newKieSession("prueba-session");


        //KnowledgeBase kbase = createKnowledgeBase();
        // create a new session
        //EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.persistence.jpa" );
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "demo:PostgresqlProject:1.0" );
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                emf );

        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();

        env.set( EnvironmentName.TRANSACTION_MANAGER,
                tm);

        KieSession kieSession = JPAKnowledgeService.newStatefulKnowledgeSession(sess.getKieBase(), null, env);

        long n = sess.getIdentifier();
        KieBase base = sess.getKieBase();

        //KieSession kieSession = JPAKnowledgeService.loadStatefulKnowledgeSession(sess.getIdentifier(), sess.getKieBase(), null, env);


        kieSession.insert("");
        kieSession.fireAllRules();

        QueryResults queryResults = kieSession.getQueryResults("elements");
        Iterator<QueryResultsRow> rows = queryResults.iterator();
        while (rows.hasNext()) {
            QueryResultsRow row = rows.next();
            res.add( row.get("element"));
        }

        res.get(0);
    }
}
