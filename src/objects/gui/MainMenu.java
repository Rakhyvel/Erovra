package objects.gui;

import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import main.Main;
import main.StateID;
import output.Render;

import com.sun.org.apache.xerces.internal.util.URI;

/**
 * Handles the main menu
 * 
 * @author Rakhyvel
 *
 */
public class MainMenu extends Menu {

	private boolean clicked = false;

	@Override
	public void tick() {
		if (Main.gameState == StateID.MENU) {
			getButtonsHovered();
			if (Main.mouse.getMouseLeftDown()) {
				if (!clicked) {
					clicked = true;
					if (buttonsHovered == 1) {
						Main.startNewMatch();
					} else if (buttonsHovered == 2) {
						Main.setState(StateID.MENU);
						try {
							openWebpage(new URL("https://github.com/Rakhyvel/Erovra/issues/new"));
						}
						catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (buttonsHovered == 3) {
						Main.endGame();
					}
				}
			} else {
				clicked = false;
			}
		}
	}

	@Override
	public void render(Render r) {
		if (Main.gameState == StateID.MENU) {
			r.drawString("Erovra", 512, 112, r.font32, 255<<24);
			drawButton("Start New Game", 512, 208, 1, r);
			drawButton("Report a Bug", 512, 256, 2, r);
			drawButton("Exit", 512, 304, 3, r);
		}
	}

	/**
	 * Gets the button that the user is hovering over with the mouse
	 */
	private void getButtonsHovered() {
		if (Main.mouse.getX() >= 402 && Main.mouse.getX() < 622 && Main.mouse.getY() > 188 && Main.mouse.getY() < 324) {
			if (Main.mouse.getY() < 228) {
				buttonsHovered = 1;
			} else if (Main.mouse.getY() >= 236 && Main.mouse.getY() < 276) {
				buttonsHovered = 2;
			} else if (Main.mouse.getY() > 284) {
				buttonsHovered = 3;
			} else {
				buttonsHovered = 0;
			}
		} else {
			buttonsHovered = 0;
		}
	}
	
	public static boolean openWebpage(java.net.URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    return false;
	}
	
	public static boolean openWebpage(URL url) {
	    try {
	        return openWebpage(url.toURI());
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
}
