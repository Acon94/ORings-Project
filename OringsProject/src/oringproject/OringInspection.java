package oringproject;

/* Sample openCV Java Application to do some simple image thresholding
 * Author: Simon McLoughlin
 * For setting up openCV for java development in Eclipse see the link below!
 * You can use version 2.4.11 of openCV as this has the javadoc as part of the download (opencv 3 does not it appears)
 * http://docs.opencv.org/2.4/doc/tutorials/introduction/java_eclipse/java_eclipse.html#java-eclipse
 * Remember to set the PATH environment variable either in windows or locally in Eclipse, to do it in eclipse:
 * Run->Run Configurations->Java Application->Your Project->Environment tab->New
 * Variable: PATH  Value: opencv bin directory. e.g C:\Users\simon\Downloads\opencv2411\opencv\build\x64\vc12\bin
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class OringInspection
{  
	
	static ArrayList points ;
	static Point min;
	static Point max;
	static int BlackLabel = 4;
	static String PF;
	static boolean result = false;
	
   public static void main( String[] args )
   {
      System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
      
      //calculate the mean processing time per frame and display it
      double before = (double)System.nanoTime()/1000000000;//secs
      
      //Create and set up the window.
      JFrame frame = new JFrame("OpenCV");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      JLabel imageHolder = new JLabel();
      JLabel histHolder = new JLabel();
      JLabel treshHolder = new JLabel();
      JLabel green = new JLabel();
      JLabel red = new JLabel();
      
      
      
     
    //  frame.getContentPane().add(imageHolder, BorderLayout.CENTER);
      frame.setLayout(new GridLayout(2, 2));
      frame.add(imageHolder);
      frame.add(histHolder);
      frame.add(treshHolder);
      
      //Display the window.
      frame.pack();
      frame.setVisible(true);
      
      //press Q to quit application
      frame.addKeyListener(new KeyListener() {
    		public void keyPressed(KeyEvent arg0) {
    			if (arg0.getKeyCode() == KeyEvent.VK_Q)
    				System.exit(0);	
    		}
    		public void keyReleased(KeyEvent arg0) {	
    		}
    		public void keyTyped(KeyEvent arg0) {
    		}
      });
      
      //String streamAddr = "http://c-cam.uchicago.edu/mjpg/video.mjpg"; //try in browser to make sure its up!
      //here is a video in the opencv installation folder!
      //String streamAddr = "C:\\Users\\simon\\Downloads\\opencv2411\\opencv\\sources\\samples\\gpu\\768x576.avi";
      
      
      
      System.out.println("Stream Opened");
      Mat img = new Mat();
      Mat out = new Mat();
      Mat histim = new Mat(256,256, CvType.CV_8UC3);
      
     
      int i=0;
      while (true) 
      {
    	  /* READ */
    	  //reading image
          img = Highgui.imread("C:\\Users\\Andrew\\Downloads\\Orings\\Oring" + (i%15 + 1) + ".jpg",0);
          
         //convert to greyscale
         // Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
          
          /* PROCESSING */
          //gets image histogram
          int [] h = hist(img);
          histim = new Mat(256,256,CvType.CV_8UC3);
          int t = findHistPeek(h)-50;
          drawHist(histim,h,t);
          
          //threshold the image
        //  int t=calcOtsu(h ,  img);
          threshold(img,findHistPeek(h)-50);
         dilate(img); 
        
         
        
         
         erode(img);
        
        label(img);
  
         
          //openCV version
          //Imgproc.threshold(img, img, 100, 255, Imgproc.THRESH_BINARY);
         
          /* DISPLAY */
          //convert to colour so we can put text into the image using whatever colour we want!
        Imgproc.cvtColor(img, out, Imgproc.COLOR_GRAY2BGR);
        double after = (double)System.nanoTime()/1000000000;//secs
        //write the text string below into the image
        Core.putText(out, "PT: " + String.format("%.4f",after-before) + " secs", new Point(10,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0,255,0));
        
        if(result == false)
        {
        	PF = "pass";
        	imageHolder.setBackground(Color.green);
        	imageHolder.setOpaque(true);
        }
        if(result == true)
        {
        	PF = "FAil";
        	result = false;
        	imageHolder.setBackground(Color.red);
        	imageHolder.setOpaque(true);
        }
        
        Core.putText(out,  PF , new Point(10,60), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0,255,0));
        
       // Imgproc.cvtColor(img, out, Imgproc.COLOR_GRAY2BGR);
       
          //write the text string below into the image
          
          //convert to a Java BufferedImage so we can display in a label
          BufferedImage jimg = Mat2BufferedImage(out/*histim*/);
          imageHolder.setIcon(new ImageIcon(jimg));
          BufferedImage Hjimg = Mat2BufferedImage(/*out*/histim);
          histHolder.setIcon(new ImageIcon(Hjimg));
          frame.pack();
          i++;
          try {
			Thread.sleep(2000);
          } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
          }
          String beforeS =""+before;
          treshHolder.setText("Processing Time: " + String.format("%.4f",after-before) + " secs" /*findHistPeek(h)*/);
        
          //System.out.println("LOOK AT THE T" + findHistPeek(h));
          frame.add(treshHolder);
          frame.pack();
      }
   }
   static void dilate(Mat img)
   {
	   byte data[] = new byte[img.rows()*img.cols()*img.channels()];
	   img.get(0, 0, data);
	   byte copy[] = data.clone();
	   for (int i=0;i<data.length;i++)
	   {
		   int [] neighbours = {i+1, i-1,i-img.cols(), i+img.cols(),i+img.cols()+1,i+img.cols()-1, i-img.cols()+1, i-img.cols()-1};
		   try{
			   for(int j=0; j<neighbours.length; j++)
			   {
				   if((copy[neighbours[j]] & 0xff)==255)
				   {
					   data[i] = (byte)255;
				   }
			   }
		   }catch(ArrayIndexOutOfBoundsException e)
		   {
			
		   }
	   }
	   img.put(0, 0, data);
   }
   
   
  /* static void dilate(Mat img)
   {
	   byte data[] = new byte[img.rows()*img.cols()*img.channels()];
	   byte [] copy = data.clone();
	   img.get(0, 0, data);
	   try{
	   for (int i=0;i<data.length;i++)
	   {
		   int unsigned = (data[i] & 0xff);
		  int [] neigh ={i+1,i-1,i-img.cols(),i+img.cols(),i-img.cols()-1,i-img.cols()+1,i+img.cols()-1,i+img.cols()+1};
		  for(int j =0;j<neigh.length;j++)
		  {
			 // System.out.println(data[j]);
			  if((copy[neigh[j]]  & 0xff )==255)
				  data[i]=(byte)255;
			  
		  }
		

	   }}
	   catch (ArrayIndexOutOfBoundsException e){}
	  
	   img.put(0, 0, data);
	   
   }
   static void erode(Mat img)
   {
	   byte data[] = new byte[img.rows()*img.cols()*img.channels()];
	   byte [] copy = data.clone();
	   img.get(0, 0, data);
	   for (int i=0;i<data.length;i++)
	   {
		   int unsigned = (data[i] & 0xff);
		  int [] neigh ={i+1,i-1,i-img.cols(),i+img.cols(),i-img.cols()-1,i-img.cols()+1,i+img.cols()-1,i+img.cols()+1};
		 try{
		  for(int j =0;j<neigh.length;j++)
		  {
			 // System.out.println(data[j]);
			  if((copy[neigh[j]]  & 0xff )==0)
				  data[i]=(byte)255;
			  
		  }
		 }
		 catch (ArrayIndexOutOfBoundsException e){}
		

	   }
	  
	   img.put(0, 0, data);
	   
   }*/
   static void erode(Mat img)
   {
	   byte data[] = new byte[img.rows()*img.cols()*img.channels()];
	   img.get(0, 0, data);
	   byte copy[] = data.clone();
	   for (int i=0;i<data.length;i++)
	   {
		   int [] neighbours = {i+1, i-1,i-img.cols(), i+img.cols(),i+img.cols()+1,i+img.cols()-1, i-img.cols()+1, i-img.cols()-1};
		   try{
			   for(int j=0; j<neighbours.length; j++)
			   {
				   if((copy[neighbours[j]] & 0xff)==0)
				   {
					   data[i] = (byte)0;
				   }
			   }
		   }catch(ArrayIndexOutOfBoundsException e)
		   {
			
		   }
	   }
	   img.put(0, 0, data);
   }
   
   static int findHistPeek(int [] hist)
   {
	   int largest =hist[0];
	   int largest_GL =0;
	   for(int i=0;i < hist.length;i++)
	   {
		   if(hist[i] > largest)
		   {
			   largest = hist[i];
			   largest_GL = i;
					   
		   }
		   
	   }
	   return largest_GL;
   }
   
   public static void threshold(Mat img, int t)
   {
	   /* threshold the image (img), note here that we need to do an
	    * & with 0xff. this is because Java uses signed two's complement
	    * types. The & operation will give us the pixel in the range we are
	    * used to, 0..255
	    */
	   byte data[] = new byte[img.rows()*img.cols()*img.channels()];
	   img.get(0, 0, data);
	   for (int i=0;i<data.length;i++)
	   {
		   int unsigned = (data[i] & 0xff);
		   if (unsigned > t)
			   data[i] = (byte)0;
		   else
			   data[i] = (byte)255;
	   }
	   img.put(0, 0, data);
   }
   
   public static int [] hist(Mat img)
   {
	   int hist [] = new int[256];
	   byte data[] = new byte[img.rows()*img.cols()*img.channels()];
	   img.get(0, 0, data);
	   for (int i=0;i<data.length;i++)
	   {
		   hist[(data[i] & 0xff)]++;
	   }
	   return hist;
   }
   
   public static void drawHist(Mat img, int [] hist,int t)
   {
	   //get max hist value for range adjustment
	   int max = 0;
	   for(int i=0;i<hist.length;i++)
	   {
		   if(hist[i] > max)
			   max = hist[i];
	   }
	   int scale = max/256;
	   for(int i=0;i<hist.length-1;i++)
	   {
		   //Core.circle(img, new Point(i*2+1,img.rows()-(hist[i]/scale)+1), 1, new Scalar(0,255,0));
		   Core.line(img, new Point(i+1,img.rows()-(hist[i]/scale)+1), new Point(i+2,img.rows()-(hist[i+1]/scale)+1), new Scalar(0,0,255));
	   }
	   Core.line(img, new Point(t,0), new Point(t,256), new Scalar(255,0,0));
   }
   public static int calctresh(int[] histData ,Mat img)
   {
	   int max;
	   int min;
	return 0;
	   
   }
   
   
   public static int calcOtsu(int[] histData ,Mat img)
   {
	   int ptr = 0;
	   byte srcData[] = new byte[img.rows()*img.cols()*img.channels()];
	   img.get(0, 0, srcData);
	   
	   while(ptr < srcData.length){
		   int h= 0xFF & srcData[ptr];
		   histData[h] ++;
		   ptr ++;
	   }
	   int total = srcData.length;
	   
	   float sum =0;
	   for(int t=0;t<256;t++) sum += t * histData[t];
	   
	   float sumB = 0;
	   int wB = 0;
	   int wF = 0;
	   
	   float varMax = 0;
	   int threshold =0;
	   
	   for(int t=0 ; t <256;t++){
		   wB += histData[t];
		   if (wB ==0);
		   
		   wF = total - wB;
		   if(wF == 0) break;
		   
		   sumB +=(float)(t * histData[t]);
		   
		   float mB = sumB / wB;
		   float mF = (sum - sumB) / wF;
		   
		   float varBetween = (float)wB * (float)wF * (mB -mF) * (mB - mF);
		   
		   if(varBetween > varMax)
		   {
			   varMax = varBetween;
			   threshold = t;
		   }
	   }
	return threshold -= 50;
	   
   }
   
   
   
   public static BufferedImage Mat2BufferedImage(Mat m)
   {
	// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
	// Fastest code
	// The output can be assigned either to a BufferedImage or to an Image

	    int type = BufferedImage.TYPE_BYTE_GRAY;
	    if ( m.channels() > 1 ) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    }
	    int bufferSize = m.channels()*m.cols()*m.rows();
	    byte [] b = new byte[bufferSize];
	    m.get(0,0,b); // get all the pixels
	    BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	    System.arraycopy(b, 0, targetPixels, 0, b.length);  
	    return image;

	}
   static void perimeter(Mat img) {
		// TODO Auto-generated method stub
	        byte data[] = new byte[img.rows() * img.cols() * img.channels()];
	        byte copy[] = new byte[img.rows() * img.cols() * img.channels()];
	        img.get(0, 0, data);

	        //Is pixel an edge pixel
	        boolean edge;

	        //Loop through all pixels
	        for (int i = 0; i < data.length; i++) {

	            if ((data[i] & 0xff) == 255) {
	                //Find all neighbouring pixels
	                int[] neighbours = {i + 1, i - 1,i - img.cols(), i + img.cols(), i - img.cols() - 1, i - img.cols() + 1, i + img.cols() - 1, i + img.cols() + 1};
	                edge = false;
	                
	                //Loop through neighbouring pixels
	                for (int j = 0; j < neighbours.length; j++) {
	                    if ((data[neighbours[j]] & 0xff) == 0) {
	                        edge = true;
	                    }
	                }
	                
	                //If edge pixel, color it white
	                if (edge==true) {
	                    copy[i] = (byte) 255;
	                }
	            }
	        }
	        
	        //Save parameter image to img
	        img.put(0, 0, copy);
	    }
   
   static int [] label(Mat img)
   {
	    Stack<Integer> checking = new Stack<Integer>();
	    

	   int currLabel = 1;
	   int blackLabel =2;
	  
	   byte data[] = new byte[img.rows()*img.cols()*img.channels()];
	   int label [] = new int [img.rows()*img.cols()*img.channels()];
	   byte [] bytearay = new byte[img.rows()*img.cols()*img.channels()];
	   
	   
	   
	   img.get(0, 0, data);
	   byte BW[]= data.clone();
	   
	   
	   for(int x=0;x<data.length;x++)
	   {
			
		
				
		try
		{
		
		if((data[x] & 0xff) ==255 && label[x]== 0)
		{
				checking.push(new Integer(x));
				
				label[x] = currLabel;
	
		while(!checking.isEmpty())
		{
			int coord = (int) checking.pop();
			if((data[coord+1] & 0xff)==255 && label[coord+1] == 0)
			{
										
				label[coord+1] = currLabel;
				checking.push(new Integer(coord+1));
					//checking.push(label[x+1]);
			}
			if((data[coord-1] & 0xff)==255 && label[coord-1] == 0)
			{
				label[coord-1] = currLabel;
				checking.push(new Integer(coord-1));					
					
			}
			if((data[coord-img.cols()] & 0xff) == 255 && label[coord-img.cols()] == 0) {
				//System.out.println("img+");
				label[coord-img.cols()] = currLabel;
				checking.push(new Integer (coord-img.cols()));
			}
			
			if((data[coord+img.cols()] & 0xff) == 255 && label[coord+img.cols()] == 0) {
				//System.out.println("img+");
				label[coord+img.cols()] = currLabel;
				checking.push(new Integer (coord+img.cols()));
			}
			
			if((data[coord+1] & 0xff)==255 && (data[coord-1] & 0xff)==0 && (data[coord+img.cols()] & 0xff) == 0 && (data[coord-img.cols()] & 0xff) == 0) {
				//System.out.println("Fail");
				result = true;
				
			}
			
			if((data[coord+1] & 0xff)==255 && (data[coord+2] & 0xff)==0 && (data[coord+3] & 0xff) == 0 && (data[coord+4] & 0xff) == 0  && (data[coord+5] & 0xff) == 255
					) {
				//System.out.println("Fail");
				result = true;
				
			}
			if((data[coord] & 0xff)==0 && (data[coord+1] & 0xff)==255 && (data[coord+2] & 0xff)==255 && (data[coord+3] & 0xff) == 255 && (data[coord+4] & 0xff) == 0 ) {
				//System.out.println("Fail");
				result = true;
				
			}
			if((data[coord-img.cols()] & 0xff)==0 && (data[coord+img.cols()] & 0xff)== 0 && (data[coord-1] & 0xff)==255 && (data[coord+1]  & 0xff) == 0 ) {
				System.out.println("Fail");
				result = true;
				
			}
			
			
		
			//System.out.println((data[coord+1 & img.cols()+2] ));//0
			//System.out.println((data[coord-img.cols() & coord+2]) );//255
		//	System.out.println((data[coord-1 & img.cols()-2]));//0
		//	System.out.println((data[coord+img.cols()+2 & coord+-1] ));//255
			
			
				
			
		
			
			
			
			
	
//			for(int i =0;i<data.length;i++)
//					{
//				int [] neighbours = {i+1, i-1,i-img.cols(), i+img.cols(),i+img.cols()+1,i+img.cols()-1, i-img.cols()+1, i-img.cols()-1};
//			
//			if( label[neighbours[i]] == 0 ) {
//				System.out.println("img-");
//				label[coord+img.cols()] = 3;
//				checking.push(new Integer(coord+img.cols()));
//			
//			}
//					}
	
			}
		currLabel++;
		}
			
		}
		catch(ArrayIndexOutOfBoundsException ex)
		{
			
		}
			/*
			  currLabel = label[x];
			  checking.push(label[x]);
			  
			  while(!checking.isEmpty())
			  {
				Object lab =  checking.get(currLabel);
				 // System.out.println("HITTNG" + lab);
				  img.put(0, 0, data);
			  }
			   
			 // System.out.println(label[x]+1);
			  
				 //  System.out.println();
			  
			
		   }
		   }
		   catch(ArrayIndexOutOfBoundsException ex)
		   {		   		   }
		   img.put(0, 0, data);*/
		
	   }
	   
	    
	 /* double mid = data.length/2;
	  System.out.println("sssss" + mid);
*/
	   boolean found = false;
	   
	   String Skey ="0110";
	   String key = data.toString();
	  // key.substring(currLabel, 4800);
//	  if( key.contains(Skey));
//	  {
//		  result=true;
//	  }
	 
	 

	 //  System.out.print(key);
	   int y =0;
	   for(int i =0 ;i<data.length;i++)
	    {
//		  
	
	    	if(y == img.cols())
	    	{
	    		System.out.println("");
	    		y=0;
	    	} 
	    System.out.print(label[i]);
	   
	    	y++;
	    }
	   System.out.println("_____________________________________________________________________________________________");
	    test(img,label);
	    corners(img,label);
	    
		  return label;
		 
		   
	   }
   public static void corners(Mat img ,int[] label)
   {
	   Stack<Integer> corners = new Stack<Integer>();
	   
	  // int [] neighbours = {i+1, i-1,i-img.cols(), i+img.cols(),i+img.cols()+1,i+img.cols()-1, i-img.cols()+1, i-img.cols()-1};
	  	  
	   byte data[] = new byte[img.rows()*img.cols()*img.channels()];
	  
	   img.get(0, 0, data);
	   byte BW[]= data.clone();
	   
	   boolean foundCor = false;
	   
	   for(int x=0;x<label.length;x++)
	   {
		   if(label[x] == 1)
		   {
			   if(label[x-1] == 1 && label[x+img.cols()] == 1  ) {
					
				  //result = true;
			   }	
	
		   }
	
		}
	   
//	   if(foundCor == true)
//	   {
//		   System.out.print("FAAILL");
//	   }
//	   else if (foundCor == false)
//	   {
//		   System.out.print("PAASS");
//	   }
	   
   }
   public static void test(Mat img ,int[] label)
   {
	  
	   
boolean found = false;
	   
	   String Skey ="0110";
	   int y =0;
	   for(int i =0 ;i<label.length;i++)
	    {
		   String Key = label.toString();
		   if(Key.contains(Skey))
		   {
			   found = true;
		   }
	    }

	   byte data[] = new byte[img.rows()*img.cols()*img.channels()];
	   byte copy[] = data.clone();
	   img.get(0,0,data);
	   
	   int checkindex =0;
	 
	   for (int i = 0; i < label.length; i++) {

		 if(label[i] == 6 )
		 {
			 data[i] = (byte)175;
		 }
		 if(label[i] ==2)
		 {
			 data[i] = (byte)0;
		 }

		 if(label[i] == 1)
		   {
			 data[i] = (byte)155;
			 
				   int [] neighbours = {i+1, i-1,i-img.cols(), i+img.cols(),i+img.cols()+1,i+img.cols()-1, i-img.cols()+1, i-img.cols()-1};
				   try{
					   for(int j=0; j<neighbours.length; j++)
					   {
						   if((label[neighbours[j]] & 0xff)==0 ) 
						   {
							   //label[i] = 2;
							   data[i] =(byte) 255;
							   
						   }

					   }
				   }catch(ArrayIndexOutOfBoundsException e)
				   {
					
				   }

		   }

	    }
	   img.put(0, 0, data);
   }
   
//   public void passfail(Mat img,int[] labels)
//   {
//	   
//	   boolean pass = false;
//	   for(int t =0;t<labels.length;t++){
//	   if(labels[t] == 2)
//	   {
//		   int col = t / img.rows();
//		   int row = t % img.cols();
//		   
//	   
//	   if(pass == false)
//	   {
//		   min = new Point(col,row);
//		   pass= true;
//	   }
//	   	points = new ArrayList<Point>();
//		points.add(new Point(col,row));
//	   
//	   }
//	   }
//   }
////	  public static Point MAxMInP()
////	  {
////		  for(int r =0 ;r < points.size();r++)
////		  {
////			  max = (Point) points.get(0);
////			  
////		  }
////		  double min = (min.x + max.x) /2 ;
////		  double max = (min.y + max.y) /2;
////		  
////		  return point;
////	  }
     	 
       
    
   }
   

	
