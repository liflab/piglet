package ca.uqac.lif.codefinder.find.sparql;

import com.github.javaparser.ast.Node;

/**
 * Utility class to generate stable IRIs for AST nodes.
 * The IRIs are based on a hash of the filename, the node's range, and its kind.
 */
class AstIds
{
	/** Namespace for AST node IRIs */
	public static final String NS = "urn:ast:";

	/**
	 * Generates a stable IRI for a given AST node.
	 * The IRI is based on a hash of the filename, the node's range, and its kind.
	 * 
	 * @param filename The name of the file containing the AST node
	 * @param n The AST node
	 * @return A stable IRI for the AST node
	 */
	public static String iriFor(String filename, Node n)
	{
		String range = n.getRange()
				.map(r -> r.begin.line + "." + r.begin.column + "-" + r.end.line + "." + r.end.column)
				.orElse("no-range");
		String kind = n.getClass().getSimpleName();
		String payload = filename + "#" + range + "#" + kind;
		String hash = sha1Hex(payload).substring(0, 16);
		return NS + hash;
	}

	/**
	 * Computes the SHA-1 hash of a string and returns it as a hexadecimal string.
	 * 
	 * @param s The input string
	 * @return The SHA-1 hash as a hexadecimal string
	 */
	public static String sha1Hex(String s)
	{
		try
		{
			var md = java.security.MessageDigest.getInstance("SHA-1");
			var b = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			var sb = new StringBuilder();
			for (byte x : b)
				sb.append(String.format("%02x", x));
			return sb.toString();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
