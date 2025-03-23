package net.koteyka32k.autoanvil;

import net.koteyka32k.autoanvil.module.AutoAnvil;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

/**
 * Auto anvil plugin
 *
 * @author Koteyka32k
 * @since long ago
 */
public class AutoAnvilPlugin extends Plugin {
	
	@Override
	public void onLoad() {
		final AutoAnvil autoAnvil = new AutoAnvil();
		RusherHackAPI.getModuleManager().registerFeature(autoAnvil);
	}
	
	@Override
	public void onUnload() {

	}
}