package net.wrap_trap.truffle_arrow;

import com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Query tests for ArrowTable
 */
public class JdbcTest {

  @BeforeClass
  public static void setupOnce() throws ClassNotFoundException {
    Class.forName("net.wrap_trap.truffle_arrow.TruffleDriver");
  }

  @Test
  public void simpleProjection() throws SQLException {
    try (Connection conn = DriverManager.getConnection("jdbc:truffle://localhost:80")) {
      PreparedStatement pstmt = conn.prepareStatement("select N_NATIONKEY, N_NAME, N_REGIONKEY from NATIONSSF");
      ResultSet rs = pstmt.executeQuery();

      ResultSetMetaData meta = rs.getMetaData();
      int count = meta.getColumnCount();
      assertThat(count, is(3));
      assertThat(meta.getColumnName(1), is("N_NATIONKEY"));
      assertThat(meta.getColumnName(2), is("N_NAME"));
      assertThat(meta.getColumnName(3), is("N_REGIONKEY"));

      List<String> results = Lists.newArrayList();
      while (rs.next()) {
        results.add(IntStream.rangeClosed(1, count)
                      .mapToObj(i -> {
                        try { return rs.getObject(i); }
                        catch (Exception e) { throw new RuntimeException(e); }
                      }).map(o -> o == null ? "" : o.toString())
                      .collect(Collectors.joining("\t")));
      }

      assertThat(results.size(), is(25));
      assertThat(results.get(0), is("0\tALGERIA\t0"));
      assertThat(results.get(24), is("24\tUNITED STATES\t1"));
    }
  }
}
