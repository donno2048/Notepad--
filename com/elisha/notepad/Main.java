package com.elisha.notepad;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.GraphicsDevice;
import java.awt.event.KeyListener;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentListener;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
class MoveListener implements CaretListener, ComponentListener, KeyListener {
	private final float SCALE = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0F;
	private final int VERTICAL_SHIFT = (int)(17.0F * this.SCALE);
	private final int HORIZONTAL_SHIFT = (int)(7.0F * this.SCALE);
	private final JFrame frame;
	private final JTextArea area;
	private final Rectangle bounds = getFullBounds();
	boolean lockEvents;
	int lastCaretPosition;
	int lastLineCaretXPosition;
	int lastLineOfCaret;
	String originalString;
	Point originalLocation;
	int accumMoveX;
	int accumMoveY;
	private Rectangle getFullBounds() {
		Rectangle rectangle = new Rectangle();
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] arrayOfGraphicsDevice = graphicsEnvironment.getScreenDevices();
		for (byte b = 0; b < arrayOfGraphicsDevice.length; b++) {
			GraphicsDevice graphicsDevice = arrayOfGraphicsDevice[b];
			GraphicsConfiguration[] arrayOfGraphicsConfiguration = graphicsDevice.getConfigurations();
			for (byte b1 = 0; b1 < arrayOfGraphicsConfiguration.length; b1++)
				rectangle = rectangle.union(arrayOfGraphicsConfiguration[b1].getBounds());
		}
		return rectangle;
	}
	MoveListener(JFrame paramJFrame, JTextArea paramJTextArea) {
		this.lockEvents = false;
		this.lastCaretPosition = 0;
		this.lastLineCaretXPosition = 0;
		this.lastLineOfCaret = 0;
		this.originalString = "";
		this.accumMoveX = 0;
		this.accumMoveY = 0;
		this.frame = paramJFrame;
		this.area = paramJTextArea;
		this.originalLocation = paramJFrame.getLocation();
	}
	public void caretUpdate(CaretEvent paramCaretEvent) {
		if (this.lockEvents)
			return;
		int i = this.area.getCaretPosition();
		if (this.lastCaretPosition == i)
			return;
		int j = getCaretY();
		int k = getCaretX();
		int m = this.frame.getX() - this.HORIZONTAL_SHIFT * (k - this.lastLineCaretXPosition);
		int n = this.frame.getY() - this.VERTICAL_SHIFT * (j - this.lastLineOfCaret);
		if (m < 0 || m + this.frame.getWidth() >= this.bounds.width || n < 0 || n + this.frame.getHeight() >= this.bounds.height) {
			Toolkit.getDefaultToolkit().beep();
			this.lockEvents = true;
			SwingUtilities.invokeLater(() -> {
				Toolkit.getDefaultToolkit().beep();
				this.area.setText(this.originalString);
				if (this.lastCaretPosition != i)
					this.area.setCaretPosition(this.lastCaretPosition);
			});
		} else {
			this.accumMoveX = 0;
			this.accumMoveY = 0;
			this.lockEvents = true;
			this.frame.setLocation(m, n - 5);
			this.originalLocation = this.frame.getLocation();
			this.lastCaretPosition = i;
			this.lastLineCaretXPosition = k;
			this.lastLineOfCaret = j;
			this.originalString = this.area.getText();
			SwingUtilities.invokeLater(() -> {
				try {
					Thread.sleep(25L);
				} catch (InterruptedException interruptedException) {}
				this.frame.setLocation(m, n);
				try {
					Thread.sleep(25L);
				} catch (InterruptedException interruptedException) {}
			});
		}
		SwingUtilities.invokeLater(() -> this.lockEvents = false);
	}
	private int getCaretX() {
		try {
			return this.area.getCaretPosition() - this.area.getLineStartOffset(this.area.getLineOfOffset(this.area.getCaretPosition()));
		} catch (BadLocationException badLocationException) {
			return 0;
		}
	}
	private int getCaretY() {
		try {
			return this.area.getLineOfOffset(this.area.getCaretPosition());
		} catch (BadLocationException badLocationException) {
			return 0;
		}
	}
	public void componentResized(ComponentEvent paramComponentEvent) {}
	public void componentMoved(ComponentEvent paramComponentEvent) {
		if (paramComponentEvent.getComponent() == this.frame) {
			if (this.lockEvents)
				return;
			Point point = this.frame.getLocation();
			int i = point.x - this.originalLocation.x;
			int j = point.y - this.originalLocation.y;
			this.accumMoveX += i;
			this.accumMoveY += j;
			while (this.accumMoveX >= this.HORIZONTAL_SHIFT) {
				this.accumMoveX -= this.HORIZONTAL_SHIFT;
				if (getCaretX() > 0) {
					this.lockEvents = true;
					int k = this.area.getCaretPosition() - 1;
					this.area.setCaretPosition(k);
					this.lastCaretPosition = k;
					this.lastLineCaretXPosition = getCaretX();
					this.lastLineOfCaret = getCaretY();
				}
			}
			while (this.accumMoveX <= -this.HORIZONTAL_SHIFT) {
				this.accumMoveX += this.HORIZONTAL_SHIFT;
				if (this.area.getCaretPosition() < getEndOfLineOffset(getCaretY()) - 1) {
					this.lockEvents = true;
					int k = this.area.getCaretPosition() + 1;
					this.area.setCaretPosition(k);
					this.lastCaretPosition = k;
					this.lastLineCaretXPosition = getCaretX();
					this.lastLineOfCaret = getCaretY();
				}
			}
			while (this.accumMoveY >= this.VERTICAL_SHIFT) {
				this.accumMoveY -= this.VERTICAL_SHIFT;
				moveCaretUpLine();
			}
			while (this.accumMoveY <= -this.VERTICAL_SHIFT) {
				this.accumMoveY += this.VERTICAL_SHIFT;
				moveCaretDownLine();
			}
			this.originalLocation = point;
			SwingUtilities.invokeLater(() -> this.lockEvents = false);
		}
	}
	private int getEndOfLineOffset(int i) {
		try {
			if (i == this.area.getLineCount() - 1)
				return this.area.getText().length() + 1;
			return this.area.getLineEndOffset(i);
		} catch (BadLocationException badLocationException) {
			return 0;
		}
	}
	private void moveCaretUpLine() {
		int i = this.area.getCaretPosition();
		int j = getCaretX();
		int k = getCaretY();
		if (k == 0)
			return;
		try {
			int m = this.area.getLineStartOffset(k - 1);
			int n = this.area.getLineEndOffset(k - 1);
			if (m + j >= n) {
				this.lockEvents = true;
				this.area.setCaretPosition(n - 1);
			} else {
				this.lockEvents = true;
				this.area.setCaretPosition(m + j);
			}
			if (this.area.getCaretPosition() != i) {
				this.lastCaretPosition = this.area.getCaretPosition();
				this.lastLineCaretXPosition = getCaretX();
				this.lastLineOfCaret = getCaretY();
			}
		} catch (BadLocationException badLocationException) {
			return;
		}
	}
	private void moveCaretDownLine() {
		int i = this.area.getCaretPosition();
		int j = getCaretX();
		int k = getCaretY();
		if (k == this.area.getLineCount() - 1) {
			if (j > 0)
				return;
			this.lockEvents = true;
			this.area.append("\n");
			this.area.setCaretPosition(this.area.getText().length());
			this.lastCaretPosition = this.area.getCaretPosition();
			this.lastLineCaretXPosition = getCaretX();
			this.lastLineOfCaret = getCaretY();
			return;
		}
		try {
			int m = this.area.getLineStartOffset(k + 1);
			int n = this.area.getLineEndOffset(k + 1);
			if (m + j > n) {
				this.lockEvents = true;
				this.area.setCaretPosition(n - 1);
			} else {
				this.lockEvents = true;
				this.area.setCaretPosition(m + j);
			}
			if (this.area.getCaretPosition() != i) {
				this.lastCaretPosition = this.area.getCaretPosition();
				this.lastLineCaretXPosition = getCaretX();
				this.lastLineOfCaret = getCaretY();
			}
		} catch (BadLocationException badLocationException) {
			badLocationException.printStackTrace();
			return;
		}
	}
	public void componentShown(ComponentEvent paramComponentEvent) {}
	public void componentHidden(ComponentEvent paramComponentEvent) {}
	public void keyPressed(KeyEvent paramKeyEvent) {
		if (paramKeyEvent.isControlDown() && paramKeyEvent.getKeyCode() == 83) {
			final JFileChooser SaveAs = new JFileChooser();
			SaveAs.setApproveButtonText("Save");
			if (SaveAs.showOpenDialog(this.area) != JFileChooser.APPROVE_OPTION)
				return;
			File fileName = new File(SaveAs.getSelectedFile().getAbsolutePath());
			BufferedWriter wr = null;
			try {
				wr = new BufferedWriter(new FileWriter(fileName));
				this.area.write(wr);
			} catch (IOException ex) {} finally {
				try {
					wr.close();
				} catch (IOException e) {}
			}
		}
	}
	public void keyReleased(KeyEvent paramKeyEvent) {}
	public void keyTyped(KeyEvent paramKeyEvent) {}
}
public final class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(Main::initInSwingEDT);
	}
	public static void initInSwingEDT() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(1);
		}
		JFrame jFrame = new JFrame("Notepad--");
		jFrame.setDefaultCloseOperation(3);
		JTextArea jTextArea = new JTextArea();
		jTextArea.setLineWrap(false);
		jFrame.add(jTextArea);
		MoveListener moveListener = new MoveListener(jFrame, jTextArea);
		jTextArea.addCaretListener(moveListener);
		jTextArea.addKeyListener(moveListener);
		jFrame.addComponentListener(moveListener);
		jFrame.setVisible(true);
		jFrame.setSize(640, 480);
		jFrame.setLocationRelativeTo((Component)null);
	}
}