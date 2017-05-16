package descrete_gulls;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.util.GraphicUtilities;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;

@ScriptManifest(author = "Tasemu", info = "Kill seagulls at Port Sarim", name = "Descrete Gulls", version = 0, logo = "")
public class main extends Script {
	private Area docks = new Area(
	    new int[][]{
	        { 3026, 3241 },
	        { 3030, 3241 },
	        { 3030, 3238 },
	        { 3047, 3238 },
	        { 3047, 3234 },
	        { 3030, 3234 },
	        { 3030, 3211 },
	        { 3026, 3211 },
	        { 3026, 3216 },
	        { 3018, 3216 },
	        { 3018, 3220 },
	        { 3026, 3220 }
	    }
	);
	private NPC target;
	private enum State {
		WAIT,
		WALK,
		ATTACK
	};

	@Override
	public void onStart() {
		log("Welcome to Descrete Gulls.");
		log("version: " + getVersion());
	}

	private State getState() {
		NPC gull = getNpcs().closest("Seagull");
		
		if (target != null || (combat.isFighting() && combat.isAutoRetaliateOn()))
			return State.WAIT;
		
		if (!docks.contains(myPlayer()))
			return State.WALK;
		
		if (
			gull != null &&
			!gull.isUnderAttack() &&
			!gull.isHitBarVisible()
		)
			return State.ATTACK;
		
		return State.WAIT;
	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
		case ATTACK:
			target = getNpcs().closest("Seagull");
			if (target != null) {
				if (target.interact("Attack")) {
					new ConditionalSleep(5000) {
						@Override
						public boolean condition() throws InterruptedException {
							return target.exists() &&
								   target.getHealthPercent() > 0;
						}
					}.sleep();
				}
			}
			break;
		case WALK:
			getWalking().webWalk(docks);
			break;
		case WAIT:
			sleep(random(500, 700));
			break;
		}
		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Bye!");
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.drawString("Status:  " + getState(), 10, 40);
		if (target != null && target.exists()) {
			GraphicUtilities.drawWireframe(getBot(), g, target);
		}
	}

}
