/*
 * 
 */

package game;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ImmuneSystem extends Applet implements MouseListener, Runnable
{
	Image buffer;
	Graphics gg;
	
	int t = 0;
	
	int mode = 0;
	boolean firstPathogenSpawned = false;
	
	int pending = 0;
	ArrayList<int[]> antibodies = new ArrayList<int[]>(); // associated pathogen
	ArrayList<int[]> macrophages = new ArrayList<int[]>(); // whether it has triangles
	ArrayList<int[]> thelpers = new ArrayList<int[]>(); // whether it has triangles
	ArrayList<int[]> tkillers = new ArrayList<int[]>(); // activated or not
	ArrayList<int[]> bplasmas = new ArrayList<int[]>(); // number of antibodies released
	ArrayList<int[]> bmemories = new ArrayList<int[]>();
	ArrayList<int[]> bcells = new ArrayList<int[]>(); // compatibility with lymphocyte array
	ArrayList<int[]> pathogens = new ArrayList<int[]>(); // number of attached antibodies

	Random rand = new Random();
	
	private Object lock = new Object();
	
	public void init()
	{
		setSize(1200, 600);
		
		addMouseListener(this);

		buffer = createImage(getWidth(), getHeight());
		gg = buffer.getGraphics();
	}
	
	public void update(Graphics g)
	{
		paint(g);
	}
	
	public void paint(Graphics g)
	{
		gg.setColor(getBackground());
		gg.fillRect(0, 0, getWidth(), getHeight());
		gg.setColor(Color.black);
		
		if (mode == 0)
			title();
		else if (mode == 1)
			game();
		else if (mode == 2)
			endScreen();
		
		g.drawImage(buffer, 0, 0, this);
	}
	
	public void title()
	{
		gg.setFont(new Font("Times Roman", Font.BOLD, 24));
		gg.drawString("Immunity War Instructions", 450, 50);
		
		gg.setFont(new Font("Times Roman", Font.BOLD, 16));
		gg.drawString("Macrophage", 50, 100);
		gg.drawString("Helper T Cell", 50, 175);
		gg.drawString("Killer T Cell", 50, 255);
		gg.drawString("B Cell", 50, 330);
		gg.drawString("Memory B Cell", 50, 400);
		gg.drawString("Plasma B Cell", 50, 475);
		gg.drawString("Antibody", 50, 540);
		
		gg.setColor(Color.gray);
		gg.fillOval(925, 110, 35, 35);
		gg.setColor(Color.green);
		gg.fillOval(880, 185, 25, 25);
		gg.setColor(Color.red);
		gg.fillOval(750, 260, 35,35);
		gg.setColor(Color.black);
		gg.drawRect(725, 335, 30, 30);
		gg.setColor(Color.yellow);
		gg.fillRect(850, 405, 30, 30);
		gg.setColor(Color.magenta);
		gg.fillRect(715, 480, 30, 30);
		gg.setColor(Color.pink);
		gg.fillRect(930, 550, 15, 15);
		
		gg.setColor(Color.BLACK);
		gg.drawRect(850, 25, 200, 35);
		gg.drawString("Ready! ---->", 900, 47);
		
		gg.setFont(new Font("Times Roman", Font.PLAIN, 12));
		gg.drawString("Macrophages are always patrolling the body. After destroying a pathogen, "
				+ "it displays antigens on its exterior. ", 50, 115);
		gg.drawString("The tank then seeks out the next pathogen, but along the way, it may drop off its antigens "
				+ "to a nearby helper T cell. ", 50, 130);
		
		gg.drawString("Once a helper T cell has obtained antigens from the macrophage, it locates the nearest "
				+ "killer T cell or B cell to pass the vial off to. ", 50, 185);
		gg.drawString("The helper T cell continues this process, constantly transferring antigens from the macrophages"
				+ "to more specialized cells. ", 50, 200);
		gg.drawString("Note: In reality, helper T cells rapidly replicate upon being presented with an antigen by a macrophage. "
				, 50, 215);
		gg.drawString("However, to make this simulation more manageable, I have omitted this feature. ", 50, 230);
		
		gg.drawString("Once a killer T cell has been activated by an antigen, "
				+ "it seeks out and ", 50, 270);
		gg.drawString("destroys any pathogen that fits the messenger's description. ", 50, 285);
		gg.drawString("Note: Similar to above", 50, 300);
		
		gg.drawString("B cells remains dormant until activated by a helper T cell. Upon activation, the B cell splits and "
				+ "adopts several different roles: ", 50, 345);
		gg.drawString("memory B cells and plasma B cells", 50, 360);
		
		gg.drawString("Memory B cells store valuable information "
				+ "on previous pathogens and can rapidly transform into different ", 50, 415);
		gg.drawString("B cells to fill other roles. However, its primary purpose is the long term storage "
				+ "of pathogen intel. ", 50, 430);
		
		gg.drawString("The plasma B cell generates antibodies specifically designed to attach to the current pathogen.", 
				50, 490);
		
		gg.drawString("Antibodies slow down the growth of a disease. A small group of drones "
				+ "is capable of immobilizing a pathogen ", 50, 555);
		gg.drawString("and preventing it from replicating. ", 50, 570);
		
	}
	
	public void game()
	{
		gg.drawString("Pathogen count: " + pathogens.size(), 50, 50);
		
		if (pending != 0)
		{
			gg.setColor(Color.red);
			gg.drawRect(300, getHeight() - 51, 50, 50);
			gg.drawString("Cancel", 300, getHeight() - 25);
			
			gg.setColor(Color.BLACK);
			gg.setFont(new Font("Times Roman", Font.PLAIN, 24));
			
			if (pending == 1)
				gg.drawString("Place Macrophage", 500, 100);
			else if (pending == 2)
				gg.drawString("Place T Helper", 510, 100);
			else if (pending == 3)
				gg.drawString("Place T Killer", 510, 100);
			else if (pending == 4)
				gg.drawString("Place B Plasma", 510, 100);
			else if (pending == 5)
				gg.drawString("Place B Memory", 510, 100);
			else if (pending == 6)
				gg.drawString("Place B Cell", 520, 100);
		}
		
		gg.setFont(new Font("Times Roman", Font.PLAIN, 10));
		
		gg.drawRect(0, getHeight() - 51, 50, 50);
		gg.drawString("Macro-", 0, getHeight() - 25);
		gg.drawString("phage", 0, getHeight() - 15);
		gg.drawRect(50, getHeight() - 51, 50, 50);
		gg.drawString("T Helper", 50, getHeight() - 25);
		gg.drawRect(100, getHeight() - 51, 50, 50);
		gg.drawString("T Killer", 100, getHeight() - 25);
		gg.drawRect(150, getHeight() - 51, 50, 50);
		gg.drawString("B Plamsa", 150, getHeight() - 25);
		gg.drawRect(200, getHeight() - 51, 50, 50);
		gg.drawString("B Memory", 200, getHeight() - 25);
		gg.drawRect(250, getHeight() - 51, 50, 50);
		gg.drawString("B Cell", 250, getHeight() - 25);
		
		for (int[] i : antibodies)
		{
			gg.setColor(Color.PINK);
			gg.fillRect(i[0]-3, i[1]-3, 6, 6);
		}
		for (int[] i : macrophages)
		{
			gg.setColor(Color.gray);
			gg.fillOval(i[0] - 35, i[1] - 35, 70, 70);
			if (i[2] == 1)
			{
				gg.setColor(Color.black);
				gg.fillPolygon(new int[] {i[0]-5, i[0]+5, i[0]}, new int[] {i[1]-35, i[1]-35, i[1]-43}, 3);
				gg.fillPolygon(new int[] {i[0]-5, i[0]+5, i[0]}, new int[] {i[1]+35, i[1]+35, i[1]+43}, 3);
				gg.fillPolygon(new int[] {i[0]+35, i[0]+35, i[0]+43}, new int[] {i[1]-5, i[1]+5, i[1]}, 3);
				gg.fillPolygon(new int[] {i[0]-35, i[0]-35, i[0]-43}, new int[] {i[1]-5, i[1]+5, i[1]}, 3);
				gg.fillPolygon(new int[] {i[0]+21, i[0]+29, i[0]+30}, new int[] {i[1]-29, i[1]-21, i[1]-30}, 3);
				gg.fillPolygon(new int[] {i[0]+21, i[0]+29, i[0]+30}, new int[] {i[1]+29, i[1]+21, i[1]+30}, 3);
				gg.fillPolygon(new int[] {i[0]-21, i[0]-29, i[0]-30}, new int[] {i[1]-29, i[1]-21, i[1]-30}, 3);
				gg.fillPolygon(new int[] {i[0]-21, i[0]-29, i[0]-30}, new int[] {i[1]+29, i[1]+21, i[1]+30}, 3);
			}
		}
		
		for (int[] i : thelpers)
		{
			gg.setColor(Color.green);
			gg.fillOval(i[0] - 18, i[1] - 17, 35, 35);
			if (i[2] == 1)
			{
				gg.setColor(Color.black);
				gg.fillPolygon(new int[] {i[0]-5, i[0]+5, i[0]}, new int[] {i[1]-17, i[1]-17, i[1]-25}, 3);
				gg.fillPolygon(new int[] {i[0]-5, i[0]+5, i[0]}, new int[] {i[1]+17, i[1]+17, i[1]+25}, 3);
				gg.fillPolygon(new int[] {i[0]-18, i[0]-18, i[0]-23}, new int[] {i[1]-5, i[1]+5, i[1]}, 3);
				gg.fillPolygon(new int[] {i[0]+18, i[0]+18, i[0]+23}, new int[] {i[1]-5, i[1]+5, i[1]}, 3);
				gg.fillPolygon(new int[] {i[0]+9, i[0]+16, i[0]+17}, new int[] {i[1]+16, i[1]+9, i[1]+17}, 3);
				gg.fillPolygon(new int[] {i[0]+9, i[0]+16, i[0]+17}, new int[] {i[1]-16, i[1]-9, i[1]-17}, 3);
				gg.fillPolygon(new int[] {i[0]-16, i[0]-9, i[0]-17}, new int[] {i[1]+9, i[1]+16, i[1]+17}, 3);
				gg.fillPolygon(new int[] {i[0]-16, i[0]-9, i[0]-17}, new int[] {i[1]-9, i[1]-16, i[1]-17}, 3);
			}
		}
		
		gg.setColor(Color.red);
		for (int[] i : tkillers)
		{
			gg.fillOval(i[0] - 20, i[1] - 20, 40, 40);
			gg.fillRect(i[0] - 5, i[1] - 35, 10, 20);
			gg.fillRect(i[0] - 5, i[1] + 15, 10, 20);
			gg.fillRect(i[0] - 35, i[1] - 5, 20, 10);
			gg.fillRect(i[0] + 15, i[1] - 5, 20, 10);
		}
		
		gg.setColor(Color.magenta);
		for (int[] i : bplasmas)
		{
			gg.fillRect(i[0] - 17, i[1] - 18, 35, 35);
		}
		
		gg.setColor(Color.yellow);
		for (int[] i : bmemories)
		{
			gg.fillRect(i[0] - 17, i[1] - 18, 35, 35);
		}
		
		gg.setColor(Color.black);
		for (int[] i : bcells)
		{
			gg.drawRect(i[0] - 17, i[1] - 18, 35, 35);
		}
		
		for (int[] e : pathogens)
		{
			gg.setColor(Color.black);
			gg.fillRect(e[0], e[1], 10, 10);
		}	
	}
	
	public void endScreen() {
		gg.setFont(new Font("Times Roman", Font.BOLD, 24));
		gg.drawString("YOU WIN", 500, 50);
		gg.drawString("You eliminated all the pathogens", 420, 80);
	}
	
	public void mouseClicked(MouseEvent e) 
	{
		int x = e.getX();
		int y = e.getY();
		
		if (mode == 0)
			titleMouseClicked(x, y);
		else if (mode == 1)
			gameMouseClicked(x, y);

		repaint();
	}
	
	public void titleMouseClicked(int x, int y)
	{
		if (x >= 850 && x <= 1050 && y >= 25 && y <= 60)
		{
			mode = 1;
			synchronized(lock)
			{
				lock.notify();
			}
		}
	}
	
	public void gameMouseClicked(int x, int y)
	{
		if (x >= 0 && x <= 50 && y >= getHeight() - 51 && y <= getHeight())
			pending = 1;
		else if (x >= 50 && x <= 100 && y >= getHeight() - 51 && y <= getHeight())
			pending = 2;
		else if (x >= 100 && x <= 150 && y >= getHeight() - 51 && y <= getHeight())
			pending = 3;
		else if (x >= 150 && x <= 200 && y >= getHeight() - 51 && y <= getHeight())
			pending = 4;
		else if (x >= 200 && x <= 250 && y >= getHeight() - 51 && y <= getHeight())
			pending = 5;
		else if (x >= 250 && x <= 300 && y >= getHeight() - 51 && y <= getHeight())
			pending = 6;
		else if (pending != 0 && x >= 300 && x <= 350 && y >= getHeight() - 51 && y <= getHeight())
			pending = 0;
		else if (pending != 0)
		{
			if (pending == 1)
				macrophages.add(new int[] {x, y, 0});
			else if (pending == 2)
				thelpers.add(new int[] {x, y, 0});
			else if (pending == 3)
				tkillers.add(new int[] {x,y, 0});
			else if (pending == 4)
				bplasmas.add(new int[] {x, y, 0});
			else if (pending == 5)
				bmemories.add(new int[] {x, y});
			else if (pending == 6)
				bcells.add(new int[] {x, y, 0});
		}
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void antibodies()
	{
		for (int[] e : antibodies)
		{
			double minDistance = 99999;
			int minPathogen = 0;
			
			for (int i = 0; i < pathogens.size(); i++)
			{
				double distance = Math.sqrt(Math.pow(e[0] - pathogens.get(i)[0], 2) + 
						Math.pow(e[1] - pathogens.get(i)[1], 2));
				
				if (distance < 10) 
				{
					pathogens.get(i)[2] ++;
					e[2] = i;
				}
				else if (distance < minDistance && pathogens.get(i)[2] < 4) {
					minDistance = distance;
					minPathogen = i;
				}
			}

			if (pathogens.size() == 0) break; // fck your index out of bounds errors java
			if (e[2] == -1)
			{
				e[0] -= 4.0 * (e[0] - pathogens.get(minPathogen)[0]) / minDistance;
				e[1] -= 4.0 * (e[1] - pathogens.get(minPathogen)[1]) / minDistance;
			}
		}
	}
	
	public void macrophage()
	{
		for (int[] e : macrophages)
		{
			double minDistance = 99999;
			int minPathogen = 0;
			for (int i = 0; i < pathogens.size(); i++)
			{
				double distance = Math.sqrt(Math.pow(e[0] - pathogens.get(i)[0], 2) + 
						Math.pow(e[1] - pathogens.get(i)[1],2));
				
				if (distance < 34) {
					e[2] = 1;
					for (int a = 0; a < antibodies.size(); a++)
					{
						if (antibodies.get(a)[2] == i)
							antibodies.remove(a);
					}
					pathogens.remove(i);
				}
				else if (distance < minDistance) {
					minDistance = distance;
					minPathogen = i;
				}
			}

			if (pathogens.size() == 0) break;
			e[0] -= 4.0 * (e[0] - pathogens.get(minPathogen)[0]) / minDistance;
			e[1] -= 4.0 * (e[1] - pathogens.get(minPathogen)[1]) / minDistance;
			
			for (int j = 0; j  < thelpers.size(); j++)
			{
				double distance = Math.sqrt(Math.pow(e[0] - thelpers.get(j)[0], 2) + 
						Math.pow(e[1] - thelpers.get(j)[1],2));
				
				if (distance < 45) {
					e[2] = 0;
					thelpers.get(j)[2] = 1;
				}
			}
		}
	}
	
	public void tHelper()
	{
		for (int[] e : thelpers)
		{
			double minDistance = 99999;
			
			if (e[2] == 0)
			{
				int minMacrophage = -1;
				for (int i = 0; i < macrophages.size(); i++)
				{
					double distance = Math.sqrt(Math.pow(e[0] - macrophages.get(i)[0], 2) + 
							Math.pow(e[1] - macrophages.get(i)[1], 2));
					
					if (distance < minDistance && macrophages.get(i)[2] == 1) {
						minDistance = distance;
						minMacrophage = i;
					}
				}
	
				if (minMacrophage > -1)
				{
					e[0] -= 10.0 * (e[0] - macrophages.get(minMacrophage)[0]) / minDistance;
					e[1] -= 10.0 * (e[1] - macrophages.get(minMacrophage)[1]) / minDistance;
				}
			}
			else if (e[2] == 1)
			{
				Set<int[]> set = new HashSet<int[]>();
				set.addAll(tkillers);
				set.addAll(bcells);
				ArrayList<int[]> lymphocytes = new ArrayList<int[]>(set);
				
				int minLymphocyte = -1;
				for (int i = 0; i < lymphocytes.size(); i++)
				{
					double distance = Math.sqrt(Math.pow(e[0] - lymphocytes.get(i)[0], 2) + 
							Math.pow(e[1] - lymphocytes.get(i)[1], 2));
					
					if (distance < minDistance) 
					{
						if (lymphocytes.get(i)[2] == 0)
						{
							minDistance = distance;
							minLymphocyte = i;
						}
					}
				}
	
				if (minLymphocyte > -1)
				{
					e[0] -= 10.0 * (e[0] - lymphocytes.get(minLymphocyte)[0]) / minDistance;
					e[1] -= 10.0 * (e[1] - lymphocytes.get(minLymphocyte)[1]) / minDistance;
				}
			}
		}
	}
	
	public void tKiller()
	{
		for (int e = 0; e < tkillers.size(); e++)
		{
			if (tkillers.get(e)[2] == 0)
			{
				randomMotion("tkillers", e);
				
				for (int i = 0; i < thelpers.size(); i++)
				{
					double distance = Math.sqrt(Math.pow(tkillers.get(e)[0] - thelpers.get(i)[0], 2) + 
							Math.pow(tkillers.get(e)[1] - thelpers.get(i)[1], 2));
					if (distance < 40) {
						tkillers.get(e)[2] = 1;
						thelpers.get(i)[2] = 0;
					}
				}
			}
			else if (tkillers.get(e)[2] == 1)
			{
				double minDistance = 99999;
				int minPathogen = 0;
				for (int i = 0; i < pathogens.size(); i++)
				{
					double distance = Math.sqrt(Math.pow(tkillers.get(e)[0] - pathogens.get(i)[0], 2) + 
							Math.pow(tkillers.get(e)[1] - pathogens.get(i)[1],2));
					
					if (distance < 30)
						pathogens.remove(i);
					else if (distance < minDistance) {
						minDistance = distance;
						minPathogen = i;
					}
				}

				if (pathogens.size() == 0) break;
				
				tkillers.get(e)[0] -= 7.0 * (tkillers.get(e)[0] - pathogens.get(minPathogen)[0]) / minDistance;
				tkillers.get(e)[1] -= 7.0 * (tkillers.get(e)[1] - pathogens.get(minPathogen)[1]) / minDistance;

			}
		}
		
	}
	
	public void bPlasma()
	{
		for (int i = 0; i < bplasmas.size(); i++)
		{
			if (bplasmas.get(i)[2] < 5)
			{
				double angle = Math.toRadians(rand.nextInt(360));
				int radius = rand.nextInt(100);
				antibodies.add(new int[] {(int) (bplasmas.get(i)[0] + radius*Math.cos(angle)),
						(int) (bplasmas.get(i)[1] + radius*Math.sin(angle)), -1});
				bplasmas.get(i)[2] ++;
			}
			else
			{
				bplasmas.remove(i);
			}
		}
	}
	
	public void bMemory()
	{
		for (int i = 0; i < bmemories.size(); i++)
		{
			randomMotion("bmemories", i);
		}
	}
	
	public void bCell()
	{
		for (int j = 0; j < bcells.size(); j++)
		{
			for (int i = 0; i < thelpers.size(); i++)
			{
				if (bcells.size()> 0)
				{
					double distance = Math.sqrt(Math.pow(bcells.get(j)[0] - thelpers.get(i)[0], 2) + 
							Math.pow(bcells.get(j)[1] - thelpers.get(i)[1], 2));
					if (distance < 45)
					{
						bCellDifferentiation(j);
						thelpers.get(i)[2] = 0;
					}
					else
					{
						randomMotion("bcells", j);
					}
				}
			}
			if (thelpers.size() == 0)
				randomMotion("bcells", j);
		}
	}
	
	private void randomMotion(String type, int a) 
	{
		double angle = Math.toRadians(rand.nextInt(360));
		
		if (type.equals("bcells"))
		{
			bcells.get(a)[0] += 3*Math.cos(angle);
			bcells.get(a)[1] += 3*Math.sin(angle);
		}
		
		else if (type.equals("tkillers"))
		{
			tkillers.get(a)[0] += 2*Math.cos(angle);
			tkillers.get(a)[1] += 2*Math.sin(angle);
		}
		
		else if (type.equals("bmemories"))
		{
			bmemories.get(a)[0] += 4*Math.cos(angle);
			bmemories.get(a)[1] += 4*Math.sin(angle);
		}
	}

	public void bCellDifferentiation(int a)
	{
		double angle = Math.toRadians(rand.nextInt(360));
		bmemories.add(new int[] {(int) (bcells.get(a)[0] + 60*Math.cos(angle)),
				(int) (bcells.get(a)[1] + 40*Math.sin(angle))});
		bmemories.add(new int[] {(int) (bcells.get(a)[0] + 60*Math.cos(angle + Math.PI)),
				(int) (bcells.get(a)[1] + 40*Math.sin(angle + Math.PI))});
		bplasmas.add(new int[] {(int) (bcells.get(a)[0] + 80),
				(int) (bcells.get(a)[1]), 0});
		bplasmas.add(new int[] {(int) (bcells.get(a)[0]),
				(int) (bcells.get(a)[1] + 80), 0});
		bplasmas.add(new int[] {(int) (bcells.get(a)[0] - 80),
				(int) (bcells.get(a)[1]), 0});
		bplasmas.add(new int[] {(int) (bcells.get(a)[0]),
				(int) (bcells.get(a)[1] - 80), 0});
		bcells.remove(a);
	}
	
	public void pathogenSpawn()
	{
		pathogens.add(new int[] {rand.nextInt(1190), rand.nextInt(590), 0});
		firstPathogenSpawned = true;
	}
	
	public void pathogenReplication(int a)
	{
		double angle = Math.toRadians(rand.nextInt(360));
		int originalX = pathogens.get(a)[0];
		int originalY = pathogens.get(a)[1];
		
		pathogens.add(new int[] {(int) (originalX + 40*Math.cos(angle)),
				(int) (originalY + 40*Math.sin(angle)), 0});
		pathogens.add(new int[] {(int) (pathogens.get(a)[0] + 40*Math.cos(angle + Math.PI)),
				(int) (pathogens.get(a)[1] + 40*Math.sin(angle + Math.PI)), 0});
		pathogens.remove(a);
	}
	
	public void helperTReplication(int a)
	{
		double angle = Math.toRadians(rand.nextInt(360));
		thelpers.add(new int[] {(int) (thelpers.get(a)[0] + 40*Math.cos(angle + Math.PI)),
				(int) (thelpers.get(a)[1] + 40*Math.sin(angle + Math.PI)), 0});
	}
	
	public void start() 
	{
		Thread th = new Thread(this);
		th.start();
	}
	
	public void run() 
	{
		synchronized(lock)
		{
			while (mode == 0)
			{
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		while (true)
		{
			if (mode == 1 && pathogens.size() < 1 && firstPathogenSpawned) {
				mode = 2;
			}
			
			else if (mode == 1 && (pathogens.size() >= 1 || !firstPathogenSpawned))
			{
				macrophage();
				tHelper();
				bCell();
				bMemory();
				antibodies();
				tKiller(); 
				
				if (t % 10 == 0)
					bPlasma();
				
				if (t % 20 == 0)
					pathogenSpawn();
				
				if (t % 10 == 5)
				{
					int x = rand.nextInt(pathogens.size());
					if (pathogens.get(x)[2] < 2)
					{
						pathogenReplication(x);
					}
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				t++;
				
				repaint();
			}
		}
	}
}
