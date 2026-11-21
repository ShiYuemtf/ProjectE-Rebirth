package moze_intel.projecte.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import moze_intel.projecte.PECore;
import moze_intel.projecte.platform.misc.Dist;
import moze_intel.projecte.platform.misc.DistExecutor;
import moze_intel.projecte.platform.misc.FMLEnvironment;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.SharedConstants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import org.jetbrains.annotations.Nullable;

public class ThreadCheckUpdate extends Thread {

	private static final String curseURL = "https://www.curseforge.com/minecraft/mc-mods/projecte/files";
	/**
	 * The same Forge update file that {@code mods.toml}'s {@code updateJSONURL} pointed at. Forge's {@code VersionChecker} did the fetch and the comparison itself;
	 * Fabric has no equivalent, so this thread reads the file and does the comparison. Promo keys look like {@code "1.20.1-latest"}. shortcut: only the
	 * {@code -latest} promo is honoured, the {@code -recommended} one stays unused because ProjectE only ships one line per Minecraft version.
	 */
	private static final String updateURL = "https://raw.githubusercontent.com/sinkillerj/ProjectE/mc1.20.x/update.json";
	private static final int timeoutMillis = 10_000;
	private static volatile String target = null;
	private static volatile boolean hasSentMessage = false;

	public ThreadCheckUpdate() {
		this.setName("ProjectE Update Checker Notifier");
		//Forge reached notifyPlayer from @Mod.EventBusSubscriber(Dist.CLIENT) on EntityJoinLevelEvent; the client join hook is the Fabric equivalent, and it has to
		//be hooked here (init time) because the fetch below finishes on another thread, well after the first join.
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.player != null) {
				notifyPlayer(client.player);
			}
		}));
	}

	@Override
	public void run() {
		if (!FMLEnvironment.distIsClient()) {
			//The notification is chat output for the local player, so a dedicated server has nothing to check
			return;
		}
		String current = getCurrentVersion();
		if (current == null) {
			PECore.LOGGER.info("Update check failed.");
			return;
		}
		String latest = fetchLatestVersion(current);
		if (latest == null) {
			PECore.LOGGER.info("Update check failed.");
			return;
		}
		if (isNewer(latest, current)) {
			target = latest;
		}
	}

	/**
	 * Prints the "update available" lines once per game session. The Forge build got here through {@code @Mod.EventBusSubscriber(value = Dist.CLIENT)} on an
	 * {@code EntityJoinLevelEvent} handler; shortcut: Fabric has no game bus world join event, so the client join hook registered in the constructor calls this.
	 */
	public static void notifyPlayer(LocalPlayer player) {
		if (target == null || hasSentMessage) {
			return;
		}
		hasSentMessage = true;
		player.sendSystemMessage(PELang.UPDATE_AVAILABLE.translate(target));
		player.sendSystemMessage(PELang.UPDATE_GET_IT.translate());
		player.sendSystemMessage(TextComponentUtil.build(new ClickEvent(ClickEvent.Action.OPEN_URL, curseURL), curseURL));
	}

	@Nullable
	private static String getCurrentVersion() {
		return FabricLoader.getInstance().getModContainer(PECore.MODID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse(null);
	}

	@Nullable
	private static String fetchLatestVersion(String current) {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(updateURL).openConnection();
			connection.setConnectTimeout(timeoutMillis);
			connection.setReadTimeout(timeoutMillis);
			//GitHub serves 403 to requests without a user agent
			connection.setRequestProperty("User-Agent", "ProjectE-Fabric/" + current);
			try (InputStream stream = connection.getInputStream(); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				JsonElement root = JsonParser.parseReader(reader);
				if (!root.isJsonObject()) {
					PECore.LOGGER.warn("Update file {} is not a JSON object", updateURL);
					return null;
				}
				JsonObject promos = root.getAsJsonObject().getAsJsonObject("promos");
				if (promos == null) {
					PECore.LOGGER.warn("Update file {} has no promos object", updateURL);
					return null;
				}
				String promoKey = SharedConstants.getCurrentVersion().getName() + "-latest";
				JsonElement latest = promos.get(promoKey);
				if (latest == null || !latest.isJsonPrimitive()) {
					PECore.debugLog("Update file {} has no promo for {}", updateURL, promoKey);
					return null;
				}
				return latest.getAsString();
			}
		} catch (IOException | RuntimeException e) {
			PECore.LOGGER.warn("Could not read the update file from {}", updateURL, e);
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private static boolean isNewer(String latest, String current) {
		try {
			//Fabric's Version is Comparable and parses the dotted forms ProjectE and the update file use, which is what Forge's ComparableVersion did here
			return Version.parse(latest).compareTo(Version.parse(current)) > 0;
		} catch (VersionParsingException e) {
			PECore.LOGGER.warn("Could not compare version '{}' against '{}'", latest, current, e);
			return false;
		}
	}
}
