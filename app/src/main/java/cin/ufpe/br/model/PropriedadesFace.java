package cin.ufpe.br.model;


import android.graphics.Bitmap;

public class PropriedadesFace {
	private int x;
	private int y;
	private int width;
	private int height;
	private Bitmap imageCortada;

	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public Bitmap getImageCortada() {
		return imageCortada;
	}
	public void setImageCortada(Bitmap imageCortada) {
		this.imageCortada = imageCortada;
	}
}