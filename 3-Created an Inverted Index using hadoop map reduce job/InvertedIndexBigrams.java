import java.io.*;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
public class InvertedIndexBigrams {



	public static class MyMapper extends Mapper<Object, Text, Text, Text> {

		private Text word = new Text();
		private Text docID = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String line =
			value.toString();

			String[] 
			splittedString = line.split("\t",2);

			docID
			.set(splittedString[0]);
			
			splittedString[1]=splittedString[1].toLowerCase().replaceAll("[^a-z]", " ");
			splittedString[1]=splittedString[1].replace(" +"," ");
			
			String prev = null;
			
			StringTokenizer
			tokenizer = new 
			StringTokenizer(
							splittedString[1]
							);

			while ( tokenizer.hasMoreTokens() ) {
				String curr=tokenizer.nextToken();
				if(prev!=null) {
					word.set(prev+" "+curr);
					context
					.write(word, docID);
				}
				
				prev=curr;

			}
		}
	}

	public static class MyReducer extends Reducer<Text, Text, Text, Text> {


		private Text result = new Text();

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			HashMap<String, Integer> hashMap =
					new HashMap<String, Integer>();

			for (Text value : 
				values) {

				String docId = 
						value.toString();

				if (hashMap.
						containsKey(docId)) {

					hashMap.
					put(docId, hashMap.get(docId) + 1);

				} 
				else {

					hashMap.
					put(docId, 1);

				}
			}

			StringBuilder stringBuilder = 
					new StringBuilder();
			for (Map.Entry<String, Integer> map : 
				hashMap.entrySet()) {

				stringBuilder.append(map.getKey());stringBuilder
				.append(":");stringBuilder.append(map.getValue());stringBuilder.append("\t");

			}
			result.
			set(stringBuilder.toString());

			context.
			write(key, result);
		}
	}
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

		if (args.length != 2) {
			System.err.println("Usage: InvertedIndexJob <input path> <output path>");
			System.exit(-1);
		}
		Configuration configuration = new Configuration();
		Job job = Job.getInstance(configuration, "Hadoop MapReduce Inverted Index");
		job.setJarByClass(InvertedIndexBigrams.class);
		job.setMapperClass(MyMapper.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		job.setReducerClass(MyReducer.class);
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}


}