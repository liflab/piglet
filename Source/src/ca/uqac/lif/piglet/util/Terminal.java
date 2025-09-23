package ca.uqac.lif.piglet.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

public class Terminal
{
	// Known DA substrings / brand markers for common SIXEL-capable terminals
	private static final Set<String> SIXEL_CAPABLE_HINTS = Set.of(
			// Many xterm builds reply with something like ESC[?62;...c or similar
			"[?62;", "[?63;", // xterm variants (version-dependent)
			// mlterm is commonly used for SIXEL
			"mlterm",
			// wezterm secondary DA includes "WezTerm"
			"WezTerm",
			// foot often includes "foot"
			"foot",
			// mintty often includes "mintty"
			"mintty",
			// contour
			"contour",
			// kitty,
			"xterm-kitty",
			// DEC VT3xx class hardware
			"[?24;", "[?41;", // common DEC capability codes for VT3xx families
			// tmux may proxy; rely on TERM_PROGRAM if present
			"tmux" // not proof of sixel, but helps you branch to TERM_PROGRAM below
	);

	/** Quick check: Console must exist (not perfect but good heuristic). */
	public static boolean isInteractive()
	{
		return System.console() != null;
	}

	/** Read DA (primary + secondary), with raw-ish input and a short timeout. */
	public static String queryDeviceAttributes(Duration timeout)
	{
		try
		{
			// Put terminal into noncanonical/no-echo using stty (best effort, UNIX only).
			String saved = execCapture("sh", "-lc", "stty -g");
			exec("sh", "-lc", "stty raw -echo");

			try
			{
				OutputStream out = System.out;
				InputStream in = System.in;

				// Primary DA request: ESC [ c
				out.write("\u001B[c".getBytes(StandardCharsets.US_ASCII));
				out.flush();
				// Secondary DA request: ESC [ > c (some terminals brand themselves here)
				out.write("\u001B[>c".getBytes(StandardCharsets.US_ASCII));
				out.flush();

				long deadline = System.nanoTime() + timeout.toNanos();
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				while (System.nanoTime() < deadline)
				{
					if (in.available() > 0)
					{
						buf.write(in.read());
					}
					else
					{
						Thread.sleep(10);
					}
				}
				return buf.toString(StandardCharsets.US_ASCII);
			}
			finally
			{
				// Restore cooked mode
				exec("sh", "-lc", "stty " + saved.trim());
			}
		}
		catch (Exception e)
		{
			return ""; // Fall back to env-based checks if DA probing fails
		}
	}

	public static boolean likelySupportsSixel()
	{
		String term = System.getenv("TERM").toLowerCase();
		System.out.println("TERM=" + term);
		
		/*
		// User override
		String force = System.getenv("FORCE_SIXEL");
		if (force != null && (force.equals("1") || force.equalsIgnoreCase("true")))
			return true;

		if (!isInteractive())
			return false;

		// Fast env heuristics (helpful under tmux/screen)
		String term = System.getenv("TERM");
		String termProgram = System.getenv("TERM_PROGRAM");
		String colorterm = System.getenv("COLORTERM");
		String env = ((term == null ? "" : term) + " " + (termProgram == null ? "" : termProgram) + " "
				+ (colorterm == null ? "" : colorterm)).toLowerCase(Locale.ROOT);
		if (env.contains("mlterm") || env.contains("wezterm") || env.contains("foot")
				|| env.contains("mintty") || env.contains("contour"))
		{
			return true;
		}

		// Active probe
		String da = queryDeviceAttributes(Duration.ofMillis(120));
		String norm = da.toLowerCase(Locale.ROOT);
		*/
		for (String hint : SIXEL_CAPABLE_HINTS)
		{
			if (term.contains(hint.toLowerCase(Locale.ROOT)))
			{
				System.out.println("Detected SIXEL-capable terminal via TERM hint: " + hint);
				return true;
			}
		}

		// Conservative default
		return false;
	}

	private static void exec(String... cmd) throws IOException, InterruptedException
	{
		new ProcessBuilder(cmd).inheritIO().start().waitFor();
	}

	private static String execCapture(String... cmd) throws IOException, InterruptedException
	{
		Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
		try (InputStream is = p.getInputStream())
		{
			String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			p.waitFor();
			return s;
		}
	}
}
