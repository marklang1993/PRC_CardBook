package com.swws.marklang.prc_cardbook.activity.system.inventory;

import com.swws.marklang.prc_cardbook.utility.ExternalFileUtility;
import com.swws.marklang.prc_cardbook.utility.MathUtility;
import com.swws.marklang.prc_cardbook.utility.inventory.Inventory;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public final class InventoryFileUtility extends ExternalFileUtility {

    // Singleton
    private static InventoryFileUtility mInventoryFileUtility;

    // Constants
    private static final boolean INVENTORY_DEBUG_IS_PRINT = false;
    private static final String INVENTORY_BACKUP_FILE_HEADER = "PRC_INVENTORY_BACKUP_FILE_HEADER";
    // NOTE: file header contains 2 parts: INVENTORY_BACKUP_FILE_HEADER, size


    private InventoryFileUtility() {
        super();
    }

    /**
     * Get an instance of InventoryFileUtility
     * @return
     */
    public static InventoryFileUtility getInstance() {
        // TODO: thread-safe?
        if (mInventoryFileUtility == null) {
            mInventoryFileUtility = new InventoryFileUtility();
        }
        return mInventoryFileUtility;
    }

    /**
     * Valid the backup file
     * @param fileName
     * @return 0: means not valid; integer > 0: the count of all inventories and means this file is valid
     */
    public int isValidBackupFile(String fileName) {
        // Get file reader
        BufferedReader reader = getReader(fileName);
        if (reader == null) return 0;

        // Read the file header
        String fileHeader = readLine(reader, INVENTORY_DEBUG_IS_PRINT);
        close(reader);
        if (fileHeader == null) return 0;

        // Check the header
        String[] headerTokens = fileHeader.split(",");
        if (headerTokens.length != 2) return 0; // Size of file header tokens is incorrect
        if (headerTokens[0].equals(INVENTORY_BACKUP_FILE_HEADER)) {
            // return the count of all inventories
            return Integer.valueOf(headerTokens[1]);

        } else {
            // Wrong header identifier
            return 0;
        }
    }

    /**
     * Read all inventories from a file
     * @param fileName
     * @param task
     * @return null or an array of all inventories
     */
    public Inventory[] readAllInventory(
            String fileName,
            ExternalFileOperationInventoryDatabaseTaskBase task,
            int startProgress,
            int endProgress
    ) {
        // Valid the backup file
        int totalCount = isValidBackupFile(fileName);
        if (totalCount == 0) return null;

        // Get file reader
        BufferedReader reader = getReader(fileName);
        if (reader == null) return null;

        // Skip the header
        readLine(reader, INVENTORY_DEBUG_IS_PRINT);

        // Read all inventory items
        Inventory[] allInventories = new Inventory[totalCount];
        for (int i = 0; i < totalCount; ++i) {
            String rawData = readLine(reader, INVENTORY_DEBUG_IS_PRINT);
            if (rawData == null) {
                // Reach file EOF - unexpected
                allInventories = null;
                break;
            }
            String[] tokens = rawData.split(",");
            // Validate amount of tokens
            if (tokens.length != 3) {
                // Invalid item - clear the array
                allInventories = null;
                break;
            }

            // Construct inventory object
            Inventory inventory = new Inventory();
            inventory.mSeasonID = Integer.valueOf(tokens[0]);
            inventory.mInventoryItemID = tokens[1];
            inventory.mInventoryItemCount = Integer.valueOf(tokens[2]);

            // Store this inventory item
            allInventories[i] = inventory;

            // Update progress
            int currentProgress = MathUtility.calculateCurrentProgressValue(
                    i, 0, totalCount, startProgress, endProgress);
            task.postNewProgress(currentProgress);
        }
        // Close the reader
        close(reader);

        return allInventories;
    }

    /**
     * Write all inventories to a file
     * @param fileName
     * @param allInventories
     * @param task
     * @return
     */
    public boolean writeAllInventory(
            String fileName,
            Inventory[] allInventories,
            ExternalFileOperationInventoryDatabaseTaskBase task,
            int startProgress,
            int endProgress
    ) {
        // Check inventories
        if (allInventories == null) return false;
        int totalCount = allInventories.length;
        if (totalCount == 0) return false;

        // Get file writer
        BufferedWriter writer = getWriter(fileName);
        if (writer == null) return false;

        // Write the header
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(INVENTORY_BACKUP_FILE_HEADER);
        headerBuilder.append(",");
        headerBuilder.append(totalCount);
        writeLine(writer, headerBuilder.toString(), INVENTORY_DEBUG_IS_PRINT);

        // Write all inventories
        for (int i = 0; i < totalCount; ++i) {
            Inventory inventory = allInventories[i];
            StringBuilder sb = new StringBuilder();
            sb.append(inventory.mSeasonID);
            sb.append(",");
            sb.append(inventory.mInventoryItemID);
            sb.append(",");
            sb.append(inventory.mInventoryItemCount);

            writeLine(writer, sb.toString(), INVENTORY_DEBUG_IS_PRINT);

            // Update progress
            int currentProgress = MathUtility.calculateCurrentProgressValue(
                    i, 0, totalCount, startProgress, endProgress);
            task.postNewProgress(currentProgress);
        }

        // Close the writer
        close(writer);

        return true;
    }
}
