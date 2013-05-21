package de.faustedition;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.jolbox.bonecp.BoneCPDataSource;
import de.faustedition.db.Relations;
import de.faustedition.facsimile.FacsimileStore;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.h2.H2TextRepository;
import eu.interedition.text.json.JacksonDataStreamMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DataModule extends AbstractModule {
    private static final Logger LOG = Logger.getLogger(DataModule.class.getName());

    private final File dataDirectory;

    public DataModule(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public DataSource relationalDataSource() throws IOException, SQLException {
        final DataSource dataSource = Relations.init(Relations.createDataSource(dataDirectory));
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Closing database connection pool " + dataSource);
                }
                ((BoneCPDataSource) dataSource).close();
            }
        }));
        return dataSource;
    }

    @Provides
    @Singleton
    public FacsimileStore facsimileStore() {
        return new FacsimileStore(dataSubDirectory("facsimile"));
    }

    @Provides
    @Singleton
    public GraphDatabaseService graphDataSource() {
        final EmbeddedGraphDatabase graphDatabase = new EmbeddedGraphDatabase(new File(dataDirectory, "graph").getPath());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Shutting down graph database " + graphDatabase);
                }
                graphDatabase.shutdown();
            }
        }));
        return graphDatabase;
    }

    @Provides
    @Singleton
    public H2TextRepository<JsonNode> textRepository(ObjectMapper objectMapper, DataSource dataSource) throws Exception {
        return new H2TextRepository<JsonNode>(JsonNode.class, new JacksonDataStreamMapper<JsonNode>(objectMapper), dataSource, true);
    }

    @Provides
    @Singleton
    public XMLStorage xmlStorage() {
        return new XMLStorage(dataSubDirectory("xml"));
    }

    protected File dataSubDirectory(String name) {
        final File subDirectory = new File(dataDirectory, name);
        if (!subDirectory.isDirectory() && !subDirectory.mkdirs()) {
            throw new ProvisionException(subDirectory + " is not a directory");
        }
        return subDirectory;
    }
}
