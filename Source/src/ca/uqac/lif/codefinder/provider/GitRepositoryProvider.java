package ca.uqac.lif.codefinder.provider;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.codefinder.util.CommandRunner;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.HardDisk;

public class GitRepositoryProvider extends FileSystemProvider
{
	public static String REPO_FOLDER = "/home/sylvain/Workspaces/repos";
	
	protected static final Pattern s_namePattern = Pattern.compile("/([^/]+)\\.git");
	
	protected final String m_repoName;
	
	protected final String m_repoUrl;
	
	protected int m_provided;
	
	public GitRepositoryProvider(String repo_url) throws FileSystemException
	{
		super(new HardDisk(REPO_FOLDER + "/" + getRepoName(repo_url)));
		m_repoUrl = repo_url;
		m_repoName = getRepoName(repo_url);
		String expected_folder = REPO_FOLDER + "/" + m_repoName;
		File f_expected_folder = new File(expected_folder);
		if (!f_expected_folder.exists())
		{
			CommandRunner runner = new CommandRunner(Arrays.asList(new String[] {"git", "clone", "--depth 1", expected_folder}), "");
			runner.run();
		}
	}
	
	protected static String getRepoName(String repo_url)
	{
		Matcher mat = s_namePattern.matcher(repo_url);
		if (mat.find())
		{
			return mat.group(1);
		}
		return "";
	}
}
