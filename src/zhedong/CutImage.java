package zhedong;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class CutImage {
	private static int count = 0;
	private static int test_count = 0;
	private static int maxWidth = 0;
	private static int maxheight = 0;
	//缩放倍数
	private static int scale = 5;
	private static boolean isDebug = false;
	//每隔多少个像素取一个点
	private static int step = 50;
	private static String file_name = "infantry_am_hd.png";
	//除重用
	private static HashMap<String, Boolean> images = new HashMap<>();
	
	public static void main(String[] args) {		
		try {
			
			System.out.println("程序开始  ");
			BufferedImage img = null;
			if(scale == 1) {
				img = ImageIO.read(new File(file_name));
			}else {
				Image img2 = ImageIO.read(new File(file_name));
				int tempWidth = img2.getWidth(null);
				int tempHeigth = img2.getHeight(null);
				img2 = img2.getScaledInstance(scale*tempWidth, scale*tempHeigth, Image.SCALE_DEFAULT);
				img = new BufferedImage(scale*tempWidth, scale*tempHeigth, BufferedImage.TYPE_INT_ARGB);
				Graphics gi=img.getGraphics();
			    gi.drawImage(img2,0,0,null);
			    gi.dispose();
			} 
		    maxWidth = img.getWidth(null); //得到源图宽
		    maxheight = img.getHeight(null); //得到源图长

		    if(isDebug) {
		    	System.out.println(maxWidth+","+maxheight);
		    }			    		    
		    		    
		    for(int i=0; i<maxWidth; i=i+step) {
		    	for(int j=0; j<maxheight; j=j+step) {
		    		if(img.getRGB(i,j)==0) {
		    			continue;
		    		}else {
		    			//System.out.println("第  "+count+"张图片");
		    			clipImages(img,i,j);
		    		}
		    	}
		    }
		    System.out.println("程序结束 ");								    
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void clipImages(BufferedImage img, int x, int y) throws IOException {
		HashMap<String,Integer> directions = getRect(img, x, y);
	    
	    int leftX = directions.get("leftX");
	    int topY = directions.get("topY");
	    int rightX = directions.get("rightX");
	    int bottomY = directions.get("bottomY");	
	    
	    if(images.containsKey("image_" + leftX + "_" + topY)) {
	    	return;
	    }else {
	    	images.put("image_" + leftX + "_" + topY, true);
	    	count++;
	    	System.out.println("第  "+ count +"张图片");
	    }
	    
	    int tempWidth = rightX-leftX;
	    int tempHeight = bottomY-topY;
	    
	    CropImageFilter cif = new CropImageFilter(leftX, topY, tempWidth,tempHeight);		
	    Image image = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(img.getSource(), cif));
	    if(scale != 1) {
	    	tempWidth = tempWidth/scale;
	    	tempHeight = tempHeight/scale;
	    	image = image.getScaledInstance(tempWidth, tempHeight, Image.SCALE_SMOOTH);
	    }	    		
	    BufferedImage bimg = new BufferedImage(tempWidth,tempHeight, BufferedImage.TYPE_INT_ARGB);
	    
	    Graphics gi=bimg.getGraphics();
	    gi.drawImage(image,0,0,null);
	    gi.dispose();
	    
	    ImageIO.write(bimg, "PNG", new File(".\\destination\\test" + count +".png"));
	}		
	
	private static HashMap<String,Integer> getRect(BufferedImage img, int x, int y) {
		
	    //初始化选择的矩形
		HashMap<String,Integer> directions = getRectForGivenPoint(img, x, y);				
		
		int leftX = directions.get("leftX");
	    int topY = directions.get("topY");
	    int rightX = directions.get("rightX");
	    int bottomY = directions.get("bottomY");
	    //如果sum=4时，说明在本次循环中四个方向都没有扩展，则认为此时已经是把图像全部包括进去了
	    int sum = 0;
		while(sum != 4) {
			sum = 0;
			boolean isLeft = false;
			boolean isRight = false;
			boolean isTop = false;
			boolean isBottom = false;
			//不断的向四个方向扩展矩形，直至把图像完全包括进去
			while(!isLeft || !isRight || !isTop || !isBottom) {
				if(isDebug) {
					System.out.println("leftX is : "+leftX + ",topY is : "+topY+",rightX is : "+rightX+",bottomY is : "+bottomY);
				}
				
				if(!isLeft) {
					for(int i = topY; i <= bottomY; i++) {
						if(leftX==0 || (i == bottomY-1 && img.getRGB(leftX,i)==0)) {
							isLeft = true;
							sum++;
							break;
						}else if(img.getRGB(leftX,i)==0) {
							continue;
						}else {		
							leftX = getLeftX(img, leftX, i);
							sum--;
							break;
						}
					}
				}
				if(!isRight) {
					for(int i = topY; i <= bottomY; i++) {
						if(rightX==maxWidth || (i == bottomY-1 && img.getRGB(rightX,i)==0)) {
							isRight = true;
							sum++;
							break;
						}else if(img.getRGB(rightX,i)==0) {
							continue;
						}else {		
							rightX = getRightX(img, rightX, i);
							sum--;
							break;
						}
					}
				}						
				if(!isTop) {
					for(int i = leftX; i <= rightX; i++) {
						if(topY==0 || (i == rightX-1 && img.getRGB(i,topY)==0)) {
							isTop = true;
							sum++;
							break;
						}else if(img.getRGB(i,topY)==0) {
							continue;
						}else {			
							topY = getTopY(img, i, topY);
							sum--;
							break;
						}
					}
				}
				if(!isBottom) {
					for(int i = leftX; i <= rightX; i++) {
						if(bottomY==maxheight || (i == rightX-1 && img.getRGB(i,bottomY)==0)) {
							isBottom = true;
							sum++;
							break;
						}else if(img.getRGB(i,bottomY)==0) {
							continue;
						}else {	
							bottomY = getBottomY(img, i, bottomY);
							sum--;
							break;
						}						
					}
				}
			}								
		}
		directions.put("leftX", leftX);
	    directions.put("rightX", rightX);
	    directions.put("topY", topY);
	    directions.put("bottomY", bottomY);
	    return directions;
	}
	
	private static int getLeftX(BufferedImage img, int x, int y) {
		int leftX = x;
		while(leftX!=0 && img.getRGB(leftX,y)!=0) {
	    	leftX--;
	    }
		//System.out.println("leftX is : "+leftX);
		return leftX;
	}
	
	private static int getRightX(BufferedImage img, int x, int y) {
		int rightX = x;
		try{
		while(rightX != maxWidth && img.getRGB(rightX,y)!=0) {
	    	rightX++;
	    }
		}catch(Exception e) {
			e.printStackTrace();
			System.err.println("rightX is : "+rightX);
		}
		//System.out.println("rightX is : "+rightX);
		return rightX;
	}
	
	private static int getTopY(BufferedImage img, int x, int y) {
		int topY = y;
		while(topY!=0 && img.getRGB(x,topY)!=0) {
	    	topY--;
	    }
	    //System.out.println("topY is : "+topY);
		return topY;
	}
	
	private static int getBottomY(BufferedImage img, int x, int y) {
		int bottomY = y;
		while(bottomY != maxheight && img.getRGB(x,bottomY)!=0) {
	    	bottomY++;
	    }
	    //System.out.println("bottomY is : "+bottomY);
		return bottomY;
	}
	
	private static HashMap<String,Integer> getRectForGivenPoint(BufferedImage img, int x, int y) {
		
	    HashMap<String,Integer> directions = new HashMap<>();
	    directions.put("leftX", getLeftX(img, x, y));
	    directions.put("rightX", getRightX(img, x, y));
	    directions.put("topY", getTopY(img, x, y));
	    directions.put("bottomY", getBottomY(img, x, y));
		return directions;
	}		
	
	private static void clipImageWithDirections(BufferedImage img, int leftX, int topY, int rightX, int bottomY) throws IOException {	    
	    
	    CropImageFilter cif = new CropImageFilter(leftX, topY, (rightX-leftX),(bottomY-topY));
		
	    Image image = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(img.getSource(), cif));
	    		
	    BufferedImage bimg = new BufferedImage((rightX-leftX),(bottomY-topY), BufferedImage.TYPE_INT_ARGB);
	    Graphics gi=bimg.getGraphics();
	    gi.drawImage(image,0,0,null);
	    gi.dispose();
	    
	    ImageIO.write(bimg, "PNG", new File(".\\1111\\test_count" + test_count++ +".png"));
	}
}
