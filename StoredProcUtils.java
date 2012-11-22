import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Utilities to call stored procedures on SQL Database.
 */
public class StoredProcUtils {

    private static final Logger logger = 
	    Logger.getLogger(StoredProcUtils.class.getName());

    public static final String RETURN_KEY = "#RETURN#";

    static public enum InOut {
	IN, OUT, INOUT
    }

    /**
     * a bind parameter
     */
    static public class BindParam {
	private final InOut io;
	private final int type; // java.sql.Types �Œ�`���ꂽ�^�������ԍ�
	private final Object value;

	public BindParam(InOut io, int type, Object value) {
	    this.io = (io == null) ? InOut.IN : io;
	    this.type = type;
	    this.value = value;
	}

	public StoredProcUtils.InOut getIo() {
	    return io;
	}

	public int getType() {
	    return type;
	}

	public Object getValue() {
	    return value;
	}
    }

    /**
     * Call a stored function that returns value.
     * 
     * @param conn
     *            JDBC Connection
     * @param procedureName
     *            Name of procedure or function (ex. "AAA_PKG.BBB_FUNC")
     * @param bindParams
     *            Map contains parameter name (key) and BindParam object (value)
     * @param returnType
     *            Type of return value (defined on jdbc.sql.Types)
     * @return Map contains return-value and OUT Parameter's value. If key of
     *         Map is RETURN_KEY, value is return-value of function.
     */
    static public Map<String, Object> callStoredFunction(Connection conn,
	    String procedureName, Map<String, BindParam> bindParams,
	    int returnType) {

	if (conn == null) {
	    throw new IllegalArgumentException("connection is null.");
	}
	if (procedureName == null || procedureName.equals("")) {
	    throw new IllegalArgumentException(
		    "procedureName is null or empty.");
	}

	return callStatement(conn, procedureName, bindParams, returnType);

    }

    /**
     * Call a stored procedure (without return-value)
     * 
     * @param conn
     *            JDBC Connection
     * @param procedureName
     *            Name of procedure or function (ex. "AAA_PKG.BBB_FUNC")
     * @param bindParams
     *            Map contains parameter name (key) and BindParam object (value)
     * @return Map contains return-value and OUT Parameter's value.
     */
    static public Map<String, Object> callStoredProcedure(Connection conn,
	    String procedureName, Map<String, BindParam> bindParams) {

	return callStoredFunction(conn, procedureName, bindParams, Types.NULL);
    }

    /**
     * Call a stored function that returns value.<br>
     * This method supports only functions that has no OUT-parameter.
     * 
     * @param conn
     *            JDBC Connection
     * @param procedureName
     *            Name of procedure or function (ex. "AAA_PKG.BBB_FUNC")
     * @param paramValues
     *            Map contains parameter name (key) and parameter value
     * @param returnType
     *            Type of return value (defined on jdbc.sql.Types)
     * @return Map contains return-value and OUT Parameter's value. If key of
     *         Map is RETURN_KEY, value is return-value of function.
     */
    static public Object callStoredFuncOnlyInParam(Connection conn,
	    String procedureName, Map<String, Object> paramValues,
	    int returnType) {

	Map<String, BindParam> bindParams = convertParamMap(paramValues);
	return callStoredFunction(conn, procedureName, bindParams, returnType)
		.get(RETURN_KEY);
    }

    /**
     * Call a stored procedure (without return-value)<br>
     * This method supports only functions that has no OUT-parameter.
     * 
     * @param conn
     *            JDBC Connection
     * @param procedureName
     *            Name of procedure or function (ex. "AAA_PKG.BBB_FUNC")
     * @param paramValues
     *            Map contains parameter name (key) and parameter value
     * @param returnType
     *            Type of return value (defined on jdbc.sql.Types)
     * @return Map contains return-value and OUT Parameter's value. If key of
     *         Map is RETURN_KEY, value is return-value of function.
     */
    static public void callStoredProcOnlyInParam(Connection conn,
	    String procedureName, Map<String, Object> paramValues) {

	Map<String, BindParam> bindParams = convertParamMap(paramValues);
	callStoredProcedure(conn, procedureName, bindParams);
    }

    // private methods -----------------------------------------

    static private Map<String, Object> callStatement(Connection conn,
	    String procedureName, Map<String, BindParam> bindParams,
	    int returnType) {

	final Set<Map.Entry<String, BindParam>> bpSet =
		(bindParams == null) ? new HashSet<Map.Entry<String, BindParam>>()
				    : bindParams.entrySet();
	CallableStatement stmt = null;
	try {
	    stmt = buildStmt(procedureName, bpSet, returnType, conn);
	    stmt.execute();
	    return extractOutParamValue(bpSet, stmt, (returnType != Types.NULL));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	} finally {
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    logger.warning("SQLException when close statement:"
			    + e.getMessage());
		}
	    }
	}
    }

    static private CallableStatement buildStmt(String procedureName,
	    Set<Map.Entry<String, BindParam>> bpSet, int returnType,
	    Connection conn) throws SQLException {

	final boolean isFunction = (returnType != Types.NULL);
	String stmtStr =
		"begin " + (isFunction ? "? := " : "") + procedureName + "("
			+ makeParamList(bpSet) + "); " + "end;";

	logger.info("make statement --> " + stmtStr);
	CallableStatement stmt = conn.prepareCall(stmtStr);
	putBindParams(bpSet, stmt, isFunction, returnType);
	return stmt;
    }

    static private String makeParamList(Set<Map.Entry<String, BindParam>> bpSet) {

	StringBuilder buf = new StringBuilder();
	for (final Map.Entry<String, BindParam> elem : bpSet) {
	    if (buf.length() > 0)
		buf.append(",");
	    buf.append(elem.getKey()).append(" => ");

	    BindParam v = elem.getValue();
	    if (v.getType() == Types.BOOLEAN) {
		if (v.getIo().equals(InOut.IN) && v.getType() == Types.BOOLEAN) {
		    // On Oracle database, JDBC cannot set boolean parameters.
		    // Boolean value write on literal.
		    buf.append((v.getValue() == null) ? "NULL" : (v.getValue()
			    .equals(Boolean.TRUE)) ? "TRUE" : "FALSE");
		}
	    } else {
		buf.append("?");
	    }
	}
	return buf.toString();
    }

    static private void putBindParams(Set<Map.Entry<String, BindParam>> bpSet,
	    CallableStatement stmt, boolean isFunction, int returnType)
	    throws SQLException {

	int index;
	if (isFunction) {
	    stmt.registerOutParameter(1, returnType);
	    index = 2;
	} else {
	    index = 1;
	}
	for (final Map.Entry<String, BindParam> elem : bpSet) {
	    final BindParam bp = elem.getValue();
	    if (bp.getType() != Types.BOOLEAN) {
		if (bp.getIo().equals(InOut.IN)
			|| bp.getIo().equals(InOut.INOUT)) {
		    stmt.setObject(index, bp.getValue());
		}
		if (bp.getIo().equals(InOut.OUT)
			|| bp.getIo().equals(InOut.INOUT)) {
		    stmt.registerOutParameter(index, bp.getType());
		}
		index++;
	    }
	}
    }

    static private Map<String, Object> extractOutParamValue(
	    Set<Map.Entry<String, BindParam>> bpSet, CallableStatement stmt,
	    boolean isFunction) throws SQLException {

	int index;
	Map<String, Object> res = new HashMap<String, Object>();
	if (isFunction) {
	    res.put(RETURN_KEY, stmt.getObject(1));
	    index = 2;
	} else {
	    index = 1;
	}

	for (final Map.Entry<String, BindParam> elem : bpSet) {
	    String k = elem.getKey();
	    BindParam v = elem.getValue();
	    if (!v.getIo().equals(InOut.IN)) {
		Object newV = stmt.getObject(index);
		res.put(k, newV);
		index++;
	    }
	}
	return res;
    }

    static private Map<String, BindParam> convertParamMap(
	    Map<String, Object> paramValues) {

	Map<String, BindParam> bindParams = new HashMap<String, BindParam>();

	if (paramValues != null) {
	    for (final String k : paramValues.keySet()) {
		Object v =
			(paramValues.get(k) != null) ? paramValues.get(k) : "";
		int type;
		if (v instanceof String) {
		    type = Types.VARCHAR;
		} else if (v instanceof Number) {
		    type = Types.NUMERIC;
		} else if (v instanceof Boolean) {
		    type = Types.BOOLEAN;
		} else if (v instanceof java.sql.Date) {
		    type = Types.DATE;
		} else if ((v instanceof java.util.Date)
			|| (v instanceof java.sql.Timestamp)) {
		    type = Types.TIMESTAMP;
		} else {
		    type = Types.OTHER;
		}
		bindParams.put(k, new BindParam(InOut.IN, type, v));
	    }
	}

	return bindParams;
    }

    private StoredProcUtils() {
    }
}
