package xyz.cuddlecloud.javax.github;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Github {

	private final String repositoryOwner;
	private final String repositoryName;
	private final String currentVersion;

	public Github(String repositoryOwner, String repositoryName, String currentVerison) {
		this.repositoryOwner = repositoryOwner;
		this.repositoryName = repositoryName;
		this.currentVersion = currentVerison;

		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException ex) {
		}
	}

	public String getRepositoryOwner() {
		return this.repositoryOwner;
	}

	public String getRepositoryName() {
		return this.repositoryName;
	}

	public String getCurrentVersion() {
		return this.currentVersion;
	}

	public String getLatestVersion() throws IOException {
		String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", repositoryOwner,
				repositoryName);

		@SuppressWarnings("deprecation")
		URL url = new URL(apiUrl);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();

			String line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			reader.close();

			JsonObject releaseDate = JsonParser.parseString(response.toString()).getAsJsonObject();
			return releaseDate.get("tag_name").getAsString();
		} else {
			return null;
		}
	}

	public void showOptionDialog() throws IOException {
		String latestVersion = getLatestVersion();
		Object[] options = { "Download", "No" };

		if (latestVersion != null) {
			if (!(latestVersion.equalsIgnoreCase(currentVersion))) {
				int input = JOptionPane.showOptionDialog(null, "New version is available! : " + latestVersion,
						currentVersion, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]);

				if (input == JOptionPane.OK_OPTION) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(new URI(String.format("https://github.com/%s/%s/releases/latest",
								repositoryOwner, repositoryName)));
					} catch (IOException | URISyntaxException e) {
						System.out.println(e);
					}
				}
			}
		}
	}
}
