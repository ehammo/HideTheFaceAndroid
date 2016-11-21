package cin.ufpe.br.service;

import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import cin.ufpe.br.Interfaces.CloudletDetectFaces;
import cin.ufpe.br.main.MainActivity;
import cin.ufpe.br.model.PropriedadesFace;

/***
 * 
 * @author Rafael Guinho
 *
 */
public class DetectFacesService implements CloudletDetectFaces {
	private static final String TAG="log";

	public List<PropriedadesFace> detectarFaces(String s, byte[] image){
		MatOfRect matOfRect = new MatOfRect();
		try {
			Log.d(TAG,"nao era pra eu entrar aki");
			ByteArrayInputStream in = new ByteArrayInputStream(image);
			Mat mat = new Mat();
			Utils.bitmapToMat(BitmapFactory.decodeStream(in), mat);
			CascadeService cs = new CascadeService();
			CascadeClassifier c = cs.loadCascade(0,s,null);
			matOfRect = detectarFaces(c, mat);
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			return obterDadosFaces(matOfRect);
		}
	}

	public MatOfRect detectarFaces(CascadeClassifier cascadeClassifier, Mat mat){
		MatOfRect matOfRect = new MatOfRect();
		cascadeClassifier.detectMultiScale(mat, matOfRect);
        Log.d(TAG, "Detected "+matOfRect.toArray().length+" faces");

		return matOfRect;
	}
	
	/***
	 * 
	 * @param matOfRect
	 * @return
	 */
	public List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect){
		
		List<PropriedadesFace> dados = new ArrayList<PropriedadesFace>();
		
		for (Rect rect : matOfRect.toArray()) {
			
			PropriedadesFace prop = new PropriedadesFace();
			prop.setX(rect.x);
			prop.setY(rect.y);
			prop.setHeight(rect.height);
			prop.setWidth(rect.width);
			
			dados.add(prop);

		}
		
		return dados;
	}
}
