package Classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jeasy.analysis.MMAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import ClassifierInterface.Classifier;

import textclassify.GUI;


public class BayesClassification implements Classifier{
	private String label=null;
	public String[] labelsName=null;
	public Vector<Label> labels=null;
	public Set<String> vocabulary=new HashSet<String>();
	public String trainPath=null;
	public String testPath=null;
	public int findMax(double[] values){
		double max=values[0];
		int mark=0;
		for(int i=0;i<values.length;i++){
			if(values[i]>max){
				max=values[i];
				mark=i;
			}
		}
		return mark;
	}
	public String[] sort(String[] pData, int left, int right){
		String middle,strTemp;
		int i = left;
		int j = right;
		middle = pData[(left+right)/2];
		do{
			while((pData[i].compareTo(middle)<0) && (i<right))
				i++;
			while((pData[j].compareTo(middle)>0) && (j>left))
				j--;
			if(i<=j){
				strTemp = pData[i];
				pData[i] = pData[j];
				pData[j] = strTemp;
				i++;
				j--;
			}
//			for(int k=0;k<pData.length;k++){
//				System.out.print(pData[k]+" ");
//			}
//			System.out.println();
		}while(i<j);//如果两边扫描的下标交错，完成一次排序
		if(left<j)
			sort(pData,left,j); //递归调用
		if(right>i)
			sort(pData,i,right); //递归调用
		return pData;
	}
	 Vector<String> readFile(String fileName) throws IOException, FileNotFoundException{
		File f=new File(fileName);
		InputStreamReader isr=new InputStreamReader(new FileInputStream(f),"GBK");
		char[] cbuf=new char[(int) f.length()];
		isr.read(cbuf);
		Analyzer analyzer=new MMAnalyzer();
		TokenStream tokens=analyzer.tokenStream("Contents", new StringReader(new String(cbuf)));
		Token token=null;
		Vector<String> v=new Vector<String>();
		while((token=tokens.next(new Token()))!=null){
			v.add(token.term());
		}
		return v;
//		String[] words=new String[v.size()];
//		for(int i=0;i<v.size();i++)
//			words[i]=v.elementAt(i);
//		sort(words,0,v.size()-1);
//		for(int i=0;i<words.length;i++){
//			System.out.println(words[i]);
//		}
	}
//	public static void main(String[] args) throws IOException{
//		long startTrain=System.currentTimeMillis();
//		String folderPath="文本分类语料库";
//		//process(folderPath);
//		//readFile("52.txt");
//		long endTrain=System.currentTimeMillis();
//		System.out.println("Traing costs "+(endTrain-startTrain)/1000+"s");
//		long startTest=System.currentTimeMillis();
//		String testPath="军事.txt";
//		//test(testPath);
//		long endTest=System.currentTimeMillis();
//		System.out.println("Test costs "+(endTest-startTest)/1000+"s");
//	}
	public void setTrainPath(String folderPat){
		trainPath=folderPat;
	}
	public void setTestPath(String testPat){
		testPath=testPat;
	}
	public void train() {
		File folder=new File(trainPath);
		labelsName=folder.list();
		labels=new Vector<Label>();
		for(int i=0;i<labelsName.length;i++){
			labels.add(new Label());
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();
			System.out.println("训练中:"+labelsName[i]);
			GUI.setTextArea("训练中:"+labelsName[i]);
			Vector<String> v=new Vector<String>();
			for(int j=0;j<files.length;j++){
				//System.out.print(files[j]+" ");
				try {
					v.addAll(readFile(trainPath+"\\"+labelsName[i]+"\\"+files[j]));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//把当前类别标签下的所有文档的所有单词都放入Set集合中，
			//目的是为了获得vocabulary的大小
			vocabulary.addAll(v);
			//对当前类别标签下的所有文档的所有单词进行排序，
			//目的是为了方便接下来统计各个单词的信息
			String[] allWords=new String[v.size()];
			for(int j=0;j<v.size();j++)
				allWords[j]=v.elementAt(j);
			sort(allWords,0,v.size()-1);
			//统计各个单词的信息
			String previous=allWords[0];
			double count=1;
			Map<String,WordItem> m=new HashMap<String, WordItem>();
			for(int j=1;j<allWords.length;j++){
				if(allWords[j].equals(previous))
					count++;
				else{
					m.put(previous, new WordItem(count));
					previous=allWords[j];
					count=1;
				}
			}
			labels.elementAt(i).set(m, v.size(),files.length);
		}
		//获得了vocabulary的大小后，下面才开始计算词频
		for(int i=0;i<labels.size();i++){
			@SuppressWarnings("rawtypes")
			Iterator iter=labels.elementAt(i).m.entrySet().iterator();
			while(iter.hasNext()){
				@SuppressWarnings("unchecked")
				Map.Entry<String, WordItem> entry=(Map.Entry<String, WordItem>)iter.next();
				WordItem item=entry.getValue();
				item.setFrequency(Math.log10((item.count+1)/(labels.elementAt(i).wordCount+vocabulary.size())));
			}
		}
	}
	//	static void process(String folderPath) throws IOException{
	//		
	//	}
		public void test(){
			Vector<String> v=null;
			try {
				v = readFile(testPath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double values[]=new double[labelsName.length];
			for(int i=0;i<labels.size();i++){
				double tempValue=labels.elementAt(i).documentCount;
				for(int j=0;j<v.size();j++){
					if(labels.elementAt(i).m.containsKey(v.elementAt(j))){
						tempValue+=labels.elementAt(i).m.get(v.elementAt(j)).frequency;
					}else{
						tempValue+=Math.log10(1/(double)(labels.elementAt(i).wordCount+vocabulary.size()));
					}
				}
				values[i]=tempValue;
			}
			//for(int i=0;i<values.length;i++)
				//所有的概率均取以10为底的log值
				//System.out.println(labelsName[i]+"的概率为"+values[i]);
			int maxIndex=findMax(values);
			System.out.println(testPath+" belongs to "+labelsName[maxIndex]);
			GUI.setTextArea(testPath+" belongs to "+labelsName[maxIndex]);
			label=labelsName[maxIndex];
		}
		public String getLabelName(){
			return label;
		}
}
class Label{//类别标签：体育、经济、政治等等
	//m中用来存放每个单词及其统计信息
	Map<String,WordItem> m=new HashMap<String,WordItem>();
	double wordCount;//某个类别标签下的所有单词个数
	double documentCount;//某个类别标签下的所有文档个数
	public Label() {
		this.m=null;
		this.wordCount=-1;
		this.documentCount=-1;
	}
	public void set(Map<String,WordItem> m,double wordCount,double documentCount) {
		this.m=m;
		this.wordCount=wordCount;
		this.documentCount=documentCount;
	}
}
class WordItem{//单词的统计信息包括单词的个数和词频
	double count;//单词的个数
	double frequency;//词频，它需要在得出vocabulary的大小之后才能计算
	public WordItem(double count) {
		this.count=count;
		this.frequency=-1;
	}
	public void setFrequency(double frequency){
		this.frequency=frequency;
	}
}

