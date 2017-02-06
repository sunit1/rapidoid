package org.rapidoid.platform;

/*
 * #%L
 * rapidoid-platform
 * %%
 * Copyright (C) 2014 - 2017 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.rapidoid.AuthBootstrap;
import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.deploy.AppDeployer;
import org.rapidoid.io.IO;
import org.rapidoid.log.Log;
import org.rapidoid.setup.App;
import org.rapidoid.setup.On;
import org.rapidoid.setup.Setup;
import org.rapidoid.u.U;
import org.rapidoid.util.AppInfo;
import org.rapidoid.util.Msc;

import java.awt.*;
import java.net.URI;
import java.util.List;

@Authors("Nikolche Mihajlovski")
@Since("5.3.0")
public class Platform extends RapidoidThing {

	private static AppChangeWatcher appChangeWatcher = new AppChangeWatcher();

	static void start(String[] args, @SuppressWarnings("unused") boolean defaults) {
		// Rapidoid banner
		U.print(IO.load("rapidoid.txt"));

		Log.options().prefix("[PLATFORM] ");
		Log.options().inferCaller(false);

		Msc.setPlatform(true);

		args = filterPlatformSpecificArgs(args);

		App.run(args);

		AppDeployer.bootstrap();

		if (!Setup.isAnyRunning()) {
			On.setup().activate();
		}

		AuthBootstrap.bootstrapAdminCredentials();

		appChangeWatcher.watch("/app", "app");

		openInBrowser();
	}

	private static String[] filterPlatformSpecificArgs(String[] args) {
		List<String> remainingArgs = U.list();

		for (String arg : args) {
			if (arg.startsWith("@")) {
				String appRef = arg.substring(1);
				AppDownloader.download(appRef, "/app");
				MavenUtil.findAndBuildAndDeploy("/app");

			} else {
				remainingArgs.add(arg);
			}
		}

		return remainingArgs.toArray(new String[remainingArgs.size()]);
	}

	private static void openInBrowser() {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(U.frmt("http://localhost:%s/", AppInfo.appPort)));
			}
		} catch (Exception e) {
			// do nothing
		}
	}

}