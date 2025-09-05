package ca.uqac.lif.codefinder.sparql;

import com.github.javaparser.ast.Node;

class AstIds
{
	static final String NS = "urn:ast:";

	static String iriFor(String filename, Node n)
	{
		String range = n.getRange()
				.map(r -> r.begin.line + "." + r.begin.column + "-" + r.end.line + "." + r.end.column)
				.orElse("no-range");
		String kind = n.getClass().getSimpleName();
		String payload = filename + "#" + range + "#" + kind;
		String hash = sha1Hex(payload).substring(0, 16);
		return NS + hash;
	}

	static String sha1Hex(String s)
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
