package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONObject;

import jp.vstone.RobotLib.CRobotMotion;

public class App {
	public static void main(String[] args) {
		// Servo init
		CRobotMotion motion = ServoInit.getInstanceOfCRobotMotion(Params.robotType);
		if (motion == null) {
			System.err.println("Servo was not initialized.");
			System.exit(0);
		}

		// ReadServer
		AxisController axisController = new AxisController(motion);
		PoseConverter poseConverter = null;
		if      (Params.robotType.equals("CommU")) poseConverter = new PoseConverter_CommU();
		else if (Params.robotType.equals("Sota"))  poseConverter = new PoseConverter_Sota();
		else if (Params.robotType.equals("Dog"))   poseConverter = new PoseConverter_Dog();
		else {
			System.err.println("Robot type is wrong. Current robot type is '" + Params.robotType + "'");
			System.exit(0);
		}

		Scanner scan = new Scanner(System.in);
		List<JSONObject> poseList = new ArrayList<JSONObject>();
		while (true) {
			String str = scan.next();
			if (str.equals("p")) {
				Map<Byte, Short> map = axisController.read();
				JSONObject obj = poseConverter.mapToJson(map);
				System.out.println(obj.toString());
			}
			else if (str.equals("+")) {
				Map<Byte, Short> map = axisController.read();
				JSONObject obj = poseConverter.mapToJson(map);
				poseList.add(obj);
				poseList.forEach(p -> System.out.println(p));
			}
			else if (str.equals("-")) {
				if (!poseList.isEmpty()) {
					poseList.remove(poseList.size() - 1);
					poseList.forEach(p -> System.out.println(p));
				}
			}
			else if (str.equals("r")) {
				if (!poseList.isEmpty()) {
					motion.ServoOn();
					for (JSONObject obj : poseList) {
						Map<Byte, Short> map = poseConverter.jsonToMap(obj);
						axisController.set(1000, map);
						try { Thread.sleep(1000); } catch (InterruptedException e) { }
					}
					motion.ServoOff();
				}
			}
			else if (str.equals("q")) {
				break;
			}
		}
		scan.close();

	}
}
