package net.wrap_trap.truffle_arrow;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class ProjectTest {

  @BeforeAll
  public static void setupOnce() throws ClassNotFoundException, IOException {
    Class.forName("net.wrap_trap.truffle_arrow.TruffleDriver");
    TestUtils.generateTestFiles("target/classes/samples/files/all_fields", TestDataType.CASE1);
    TruffleArrowConfig.INSTANCE.reload();
  }

  @AfterAll
  public static void teardownOnce() throws IOException {
    TestUtils.deleteDirectory("target/classes/samples/files/all_fields");
  }

  @Test
  public void simpleProject() throws SQLException {
    try (
      Connection conn = DriverManager.getConnection("jdbc:truffle:");
      PreparedStatement pstmt = conn.prepareStatement(
        "select F_BIGINT, F_VARCHAR from ALL_FIELDS where F_INT=2");
      ResultSet rs = pstmt.executeQuery()
    ) {
      List<String> results = TestUtils.getResults(rs);
      assertThat(results.size(), is(1));
      assertThat(results.get(0), is("2\ttest2"));
      assertThat(LastPlan.INSTANCE.includes(ArrowProject.class), is(true));
    }
  }

  @Test
  public void simpleAProjectAll() throws SQLException {
    try (
      Connection conn = DriverManager.getConnection("jdbc:truffle:");
      PreparedStatement pstmt = conn.prepareStatement(
        "select F_INT, F_BIGINT, F_VARCHAR, F_TIMESTAMP, F_TIME, F_DATE, F_DOUBLE from ALL_FIELDS where F_INT=2");
      ResultSet rs = pstmt.executeQuery()
    ) {
      List<String> results = TestUtils.getResults(rs);
      assertThat(results.size(), is(1));
      assertThat(results.get(0), is("2\t2\ttest2\t2020-05-04 15:48:11.0\t03:20:23\t2020-05-05\t125.456"));
      assertThat(LastPlan.INSTANCE.includes(ArrowProject.class), is(true));
    }
  }

  @Test
  public void simpleAProjectAsterisk() throws SQLException {
    try (
      Connection conn = DriverManager.getConnection("jdbc:truffle:");
      PreparedStatement pstmt = conn.prepareStatement(
        "select * from ALL_FIELDS where F_INT=2");
      ResultSet rs = pstmt.executeQuery()
    ) {
      List<String> results = TestUtils.getResults(rs);
      assertThat(results.size(), is(1));
      assertThat(results.get(0), is("2\t2\ttest2\t2020-05-04 15:48:11.0\t03:20:23\t2020-05-05\t125.456"));
      assertThat(LastPlan.INSTANCE.includes(ArrowProject.class), is(true));
    }
  }
}