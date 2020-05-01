package online.kheops.auth_server.util;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class PGDialect extends PostgreSQLDialect {

        public PGDialect() {
            super();
            registerFunction("string_agg", new SQLFunctionTemplate( StandardBasicTypes.STRING, "string_agg(distinct ?1, ?2)"));
        }
    }