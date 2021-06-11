package online.kheops.auth_server.util;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class ExtendPostgreSQLDialect extends PostgreSQLDialect {

    public ExtendPostgreSQLDialect() {
        super();
        registerFunction("array_agg", new StandardSQLFunction("array_agg", StandardBasicTypes.STRING));
        registerFunction("SOUNDEX", new StandardSQLFunction("SOUNDEX", StandardBasicTypes.LONG));
    }
}
