package com.spotify.helios.testing;

import com.google.common.base.Joiner;

import com.spotify.helios.client.HeliosClient;
import com.spotify.helios.common.descriptors.Job;
import com.spotify.helios.common.descriptors.JobId;
import com.spotify.helios.system.SystemTestBase;

import org.junit.Rule;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.spotify.helios.testing.TemporaryJobs.TemporaryJob;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.experimental.results.PrintableResult.testResult;
import static org.junit.experimental.results.ResultMatchers.isSuccessful;

public class TemporaryJobsTest extends SystemTestBase {

  // These static fields exist as a way for FakeTest to access non-static fields and methods in
  // SystemTestBase. This is a bit ugly, but we can't pass the values to FakeTest, because we don't
  // instantiate it, JUnit does in the PrintableResult.testResult method. And since JUnit
  // instantiates it, it must be a static class, which means it can't access the non-static fields
  // in SystemTestBase.
  private static HeliosClient client;
  private static String testHost;

  public static class FakeTest {

    public static final String SERVICE = "service";

    @Rule
    public final TemporaryJobs temporaryJobs = TemporaryJobs.create(client);

    private final TemporaryJob job1 = temporaryJobs.job()
        .image("ubuntu:12.04")
        .command("sh", "-c", "while :; do sleep 1; done")
        .host(testHost)
        .port(SERVICE, 4229)
        .registration("wiggum", "hm", SERVICE)
        .deploy();

    private final TemporaryJob job2 = temporaryJobs.job()
        .image("ubuntu:12.04")
        .command("sh", "-c", "while :; do sleep 1; done")
        .host(testHost)
        .port("service", 4229)
        .registration("wiggum", "hm", "service")
        .env("FOO_ADDRESS", Joiner.on(',').join(job1.addresses(SERVICE)))
        .deploy();

    @Test
    public void testDeployment() throws Exception {
      final Map<JobId, Job> jobs = client.jobs().get(15, TimeUnit.SECONDS);
      assertEquals("wrong number of jobs running", 2, jobs.size());
      for (Job job : jobs.values()) {
        assertEquals("wrong job running", "ubuntu:12.04", job.getImage());
      }
    }
  }

  @Test
  public void testRule() throws Exception {
    startDefaultMaster();
    client = defaultClient();
    testHost = getTestHost();
    startDefaultAgent(testHost);

    assertThat(testResult(FakeTest.class), isSuccessful());
    assertTrue("jobs are running that should not be",
               client.jobs().get(15, TimeUnit.SECONDS).isEmpty());
  }

}
