package org.olf.erm.usage.counter50.merger;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.olf.erm.usage.counter50.Counter5Utils;
import org.olf.erm.usage.counter50.Counter5Utils.Counter5UtilsException;

public abstract class MergerTest<T> {

  protected String prefix;

  public List<T> readData() throws IOException, Counter5UtilsException {
    String input1 = "reports/" + prefix + "_1.json";
    String input2 = "reports/" + prefix + "_2.json";
    String input3 = "reports/" + prefix + "_3.json";
    String expected = "reports/" + prefix + "_merged.json";

    URL url1 = Resources.getResource(input1);
    String jsonString1 = Resources.toString(url1, StandardCharsets.UTF_8);
    T rep1 = (T) Counter5Utils.fromJSON(jsonString1);

    URL url2 = Resources.getResource(input2);
    String jsonString2 = Resources.toString(url2, StandardCharsets.UTF_8);
    T rep2 = (T) Counter5Utils.fromJSON(jsonString2);

    URL url3 = Resources.getResource(input3);
    String jsonString3 = Resources.toString(url3, StandardCharsets.UTF_8);
    T rep3 = (T) Counter5Utils.fromJSON(jsonString3);

    URL urlExpected = Resources.getResource(expected);
    String jsonStringExpected = Resources.toString(urlExpected, StandardCharsets.UTF_8);
    T repExpected = (T) Counter5Utils
        .fromJSON(jsonStringExpected);

    return Arrays.asList(rep1, rep2, rep3, repExpected);
  }

}
