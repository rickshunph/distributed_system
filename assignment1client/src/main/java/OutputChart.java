import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutputChart {

  private static final String COMMA_DELIMITER = ",";
  private static final String CSV_FILE_NAME = "latencyTimeChartData";
  private static final String CSV_FILE_SURFIX = ".csv";

  public static void main(String[] args) throws IOException {
    List<Long> latencyGet = new ArrayList<>();
    List<Integer> requestNumGet = new ArrayList<>();

    List<Long> latencyPost = new ArrayList<>();
    List<Integer> requestNumPost = new ArrayList<>();

    int[] maxThreadSet = new int[]{32, 64, 128, 256};

    for (int maxThread : maxThreadSet) {

      // get the range of start time
      long min = Long.MAX_VALUE;
      long max = Long.MIN_VALUE;
      try (BufferedReader br = new BufferedReader(new FileReader(
          "output_csv_thread_" + maxThread + ".csv"))) {
        String line;
        while ((line = br.readLine()) != null) {
          String[] values = line.split(COMMA_DELIMITER);
          long startTime = Long.parseLong(values[0]);
          if (startTime < min) {
            min = startTime;
          } else if (startTime > max) {
            max = startTime;
          }
        }
      }

      System.out.println("max: " + max);
      System.out.println("min: " + min);

      int numSecInterval = (int) ((max / 1000) + 1 - (min / 1000));
      System.out.println("numSecInterval: " + numSecInterval);

      // start from min and end to max
      long[] latenciesGet = new long[numSecInterval];
      long[] numThreadsGet = new long[numSecInterval];
      long[] latenciesPost = new long[numSecInterval];
      long[] numThreadsPost = new long[numSecInterval];

      try (BufferedReader br = new BufferedReader(new FileReader(
          "output_csv_thread_" + maxThread + ".csv"))) {
        String line;
        while ((line = br.readLine()) != null) {
          String[] values = line.split(COMMA_DELIMITER);
          long startTime = Long.parseLong(values[0]);
          String type = values[1];
          long latency = Long.parseLong(values[2]);
          int index = (int) (startTime - min) / 1000;

          if (type.equalsIgnoreCase("GET")) {
            latenciesGet[index] += latency;
            numThreadsGet[index]++;
          } else {
            latenciesPost[index] += latency;
            numThreadsPost[index]++;
          }
        }
      }

      for (int i = 0; i < numThreadsGet.length; i++) {
        if (latenciesGet[i] != 0) {
          latenciesGet[i] /= numThreadsGet[i];
        }
        if (latenciesPost[i] != 0) {
          latenciesPost[i] /= numThreadsPost[i];
        }
      }
      System.out.println(Arrays.toString(latenciesGet));
      System.out.println(Arrays.toString(latenciesPost));

      writeCsv(latenciesGet, CSV_FILE_NAME + "_get_thread_" + maxThread +
          CSV_FILE_SURFIX);
      writeCsv(latenciesPost, CSV_FILE_NAME + "_post_thread_" + maxThread +
          CSV_FILE_SURFIX);
    }
  }

  private static void writeCsv(long[] list, String fileName) throws FileNotFoundException {
    File csvOutputFile = new File(fileName);
      try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
      StringBuilder sb = new StringBuilder();
      for (int j = 1; j <= list.length; j++) {
        sb.append(j + ",");
        sb.append(list[j - 1]);
        sb.append("\n");
      }
      pw.write(sb.toString());
    }
  }
}
