package spark.word_processor;

/**
 * Created by vsanjekar on 1/13/16.
 */

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class TupleComparator implements Comparator<Tuple2<String, Integer>>, Serializable {
    @Override
    public int compare(Tuple2<String, Integer> o1, Tuple2<String, Integer> o2) {
        return o1._2.compareTo(o2._2);
    }
}

public class WordProcessor {

    public static void main(final String[] args) {
        System.out.println("Hellow World. This is Apache Spark WordProcessor example");

        if (args.length < 0) {
            System.out.println("Specify data file");
            System.exit(0);
        }

        String inputFileName = args[0];
        final SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("SparkWordCountJava");
        sparkConf.setMaster("local[4]"); // Four threads

        final JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);

        final List<String> data = Arrays.asList(
                "This is some text to use for word count example",
                "My name is Vinay",
                "This is spark example");
        // final JavaRDD<String> javaRDDLines = javaSparkContext.parallelize(data);
        // final JavaRDD<String> javaRDDLines = javaSparkContext.textFile("4300.txt");
        final JavaRDD<String> javaRDDLines = javaSparkContext.textFile(inputFileName);

        final JavaRDD<String> javaRDDWords = javaRDDLines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());
        final JavaPairRDD<String, Integer> pairRDD = javaRDDWords.mapToPair(word -> new Tuple2<>(word, 1));
        final JavaPairRDD<String, Integer> wordCountsRDD = pairRDD.reduceByKey((a, b) -> a+b);
        String fileName = String.valueOf(System.currentTimeMillis());
        // wordCountsRDD.collect().forEach(System.out::println);
        wordCountsRDD.saveAsTextFile("target/"+fileName+"_wordcount");

        // Get the words with count more than N=10
        Function<Tuple2<String, Integer>, Boolean> greaterThanN = e -> (e._2 > 10);
        final JavaPairRDD<String, Integer> wordCountsMoreThanTenRDD = wordCountsRDD.filter(greaterThanN);
        wordCountsMoreThanTenRDD.saveAsTextFile("target/"+fileName+"_wordcount_more_than_10");

        // Get Top N=10 words
        List<Tuple2<String, Integer>> wordCountsTopTen = wordCountsRDD.top(10, new TupleComparator());
        // List<Tuple2<String, Integer>> wordCountsTopTen = wordCountsRDD.takeOrdered(10,new TupleComparator());
        final JavaRDD<Tuple2<String, Integer>> wordCountsTopTenRDD = javaSparkContext.parallelize(wordCountsTopTen);
        wordCountsTopTenRDD.saveAsTextFile("target/"+fileName+"_wordcount_top_100");

        javaSparkContext.stop();
    }
}