package com.aptana.git.internal.core.storage;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;

import com.aptana.git.core.model.ChangedFile;
import com.aptana.git.core.model.GitIndex;
import com.aptana.git.core.model.GitRepository;
import com.aptana.util.IOUtil;

public class GitFileHistoryTest extends TestCase
{

	private static final String PROJECT_NAME = "gfh_test"; //$NON-NLS-1$
	private IProject fProject;
	private GitRepository fRepo;

	public void testGetFileRevisions() throws Exception
	{
		GitRepository repo = getRepo();
		final String filename = "comitted_file.txt";

		List<String> commitsToMake = new ArrayList<String>();
		commitsToMake.add("Hello World!");
		commitsToMake.add("# Second commit contents.");

		GitIndex index = repo.index();
		// Actually add a file to the location

		String txtFile = new File(repo.workingDirectory(), filename).getAbsolutePath();
		for (String contents : commitsToMake)
		{
			FileWriter writer = new FileWriter(txtFile);
			writer.write(contents);
			writer.close();
			// refresh the index
			index.refresh();

			// Stage the new file
			int tries = 100;
			List<ChangedFile> toStage = index.changedFiles();
			// HACK Wait until we get a non-empty list?
			while (toStage == null || toStage.isEmpty())
			{
				Thread.sleep(50);
				toStage = index.changedFiles();
				tries--;
				if (tries <= 0)
					break;
			}
			assertNotNull(toStage);
			assertTrue(toStage.size() > 0);
			index.stageFiles(toStage);
			index.refresh();
			index.commit(contents);
		}

		// Normal test
		IFile resource = getProject().getFile(filename);
		GitFileHistory history = new GitFileHistory(resource, IFileHistoryProvider.NONE, null);
		IFileRevision[] revs = history.getFileRevisions();
		assertNotNull(revs);
		assertEquals(2, revs.length);
		int i = revs.length - 1;
		for (IFileRevision revision : revs)
		{
			assertTrue(revision.exists());
			IStorage storage = revision.getStorage(new NullProgressMonitor());
			assertEquals(commitsToMake.get(i--), IOUtil.read(storage.getContents()));
			// Make sure getFileRevision works as we expect
			assertSame(revision, history.getFileRevision(revision.getContentIdentifier()));
		}

		// Test getContributors
		IFileRevision[] contributors = history.getContributors(revs[0]);
		assertNotNull(contributors);
		assertEquals(1, contributors.length);
		assertSame(contributors[0], revs[1]);

		// TODO Test when there are two+ contributors!

		// Test getTargets
		IFileRevision[] targets = history.getTargets(revs[1]);
		assertNotNull(targets);
		assertEquals(1, targets.length);
		assertSame(targets[0], revs[0]);

		// TODO Test when there are two+ targets!

		// Test with a flag for single revision!
		history = new GitFileHistory(resource, IFileHistoryProvider.SINGLE_REVISION, null);
		revs = history.getFileRevisions();
		assertNotNull(revs);
		assertEquals(1, revs.length);

		// TODO Test with single line of descent flag!
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		try
		{
			if (fProject != null)
				fProject.delete(true, new NullProgressMonitor());
		}
		finally
		{
			fProject = null;
			fRepo = null;
			super.tearDown();
		}
	}

	protected GitRepository getRepo() throws CoreException
	{
		if (fRepo == null)
		{
			fRepo = createRepo();
		}
		return fRepo;
	}

	protected GitRepository createRepo() throws CoreException
	{
		GitRepository.create(getProject().getLocation().toOSString());
		return GitRepository.attachExisting(getProject(), new NullProgressMonitor());
	}

	protected IPath repoToGenerate() throws CoreException
	{
		return getProject().getLocation();
	}

	private IProject getProject() throws CoreException
	{
		if (fProject == null)
		{
			fProject = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
			if (!fProject.exists())
				fProject.create(new NullProgressMonitor());
			if (!fProject.isOpen())
				fProject.open(new NullProgressMonitor());
		}
		return fProject;
	}
}
