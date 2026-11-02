package com.firetv.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.leanback.app.LeanbackActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends LeanbackActivity {
    private static final String TAG = "FireTVSettingsApp";
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        configureButton(R.id.buttonMainSettings, "Open main Settings", Settings.ACTION_SETTINGS);
        configureButton(R.id.buttonDisplaySettings, "Display", Settings.ACTION_DISPLAY_SETTINGS);
        configureButton(R.id.buttonNetworkSettings, "Network", Settings.ACTION_WIFI_SETTINGS);
        configureButton(R.id.buttonSoundSettings, "Sound", Settings.ACTION_SOUND_SETTINGS);
        configureButton(R.id.buttonAccessibilitySettings, "Accessibility", Settings.ACTION_ACCESSIBILITY_SETTINGS);
        configureButton(R.id.buttonDeveloperSettings, "Developer Options", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        configureButton(R.id.buttonInputSettings, "Input / Keyboard", Settings.ACTION_INPUT_METHOD_SETTINGS);
        configureButton(R.id.buttonRemoteAccessories, "Remotes & Accessories", Settings.ACTION_SETTINGS);

        Button btnRootCheck = findViewById(R.id.buttonRootCheck);
        btnRootCheck.setOnClickListener(v -> {
            boolean root = hasRootAccess();
            showStatus("Root access: " + (root ? "available" : "not available"));
        });

        Button btnRootCommand = findViewById(R.id.buttonRootCommand);
        btnRootCommand.setOnClickListener(v -> {
            if (!hasRootAccess()) {
                showStatus("Root command skipped: no root access");
                return;
            }

            runRootCommand("settings list system");
            showStatus("Root shell command executed");
        });

        Button btnRemap = findViewById(R.id.buttonRemap);
        btnRemap.setOnClickListener(v -> launchRemapFallback());

        Button btnLaunchLauncher = findViewById(R.id.buttonLaunchLauncher);
        btnLaunchLauncher.setOnClickListener(v -> openPackage("com.amazon.tv.launcher"));

        showStatus("Ready for Fire OS 11 / Android TV 11");
    }

    private void configureButton(int viewId, String label, String action) {
        Button button = findViewById(viewId);
        button.setText(label);
        button.setOnClickListener(v -> openAction(action));
    }

    private void openAction(String action) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            showStatus("Opening: " + action);
        } catch (ActivityNotFoundException e) {
            showStatus("Activity not found for: " + action + "\nFalling back to full Settings");
            openAction(Settings.ACTION_SETTINGS);
        } catch (Exception e) {
            showStatus("Unable to open action: " + action + "\n" + e.getMessage());
            Log.e(TAG, "Failed to open action", e);
        }
    }

    private void openPackage(String packageName) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                showStatus("Launching package: " + packageName);
                return;
            }
            showStatus("Package not found: " + packageName);
        } catch (Exception e) {
            showStatus("Could not launch package: " + packageName + "\n" + e.getMessage());
        }
    }

    private void launchRemapFallback() {
        String[] candidateActions = {
                "android.settings.SETTINGS",
                Settings.ACTION_SETTINGS,
                Settings.ACTION_ACCESSIBILITY_SETTINGS,
                Settings.ACTION_INPUT_METHOD_SETTINGS,
                "android.settings.VOICE_INPUT_SETTINGS"
        };

        for (String candidate : candidateActions) {
            try {
                Intent intent = new Intent(candidate);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                showStatus("Remap fallback opened: " + candidate);
                return;
            } catch (Exception ignored) {
            }
        }

        showStatus("No remap screen available on this Fire TV build. Use the main settings menu to access remote / accessibility controls.");
    }

    private void showStatus(String message) {
        statusText.setText(message);
    }

    private boolean hasRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "id" });
            int exitCode = process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            return exitCode == 0 && output != null && !output.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void runRootCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", command });
            int exitCode = process.waitFor();
            StringBuilder log = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.append(line).append('\n');
            }
            if (exitCode != 0) {
                showStatus("Root command failed: " + exitCode + "\n" + log);
            } else {
                showStatus("Root command completed\n" + log);
            }
        } catch (IOException | InterruptedException e) {
            showStatus("Root command error: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
