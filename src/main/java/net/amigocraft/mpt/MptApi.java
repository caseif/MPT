package net.amigocraft.mpt;

import net.amigocraft.mpt.command.*;
import net.amigocraft.mpt.util.MPTException;

import java.util.List;

public class MptApi {

	/**
	 * Installs a package by the given ID.
	 * @param id the ID of the package to install
	 * @throws MPTException if something goes wrong while downloading or installing the package.
	 * A specific error message will be included in the exception.
	 */
	public static void installPackage(String id) throws MPTException {
		InstallCommand.installPackage(id);
	}

	/**
	 * Remove a package by the given ID.
	 * @param id the ID of the package to install
	 * @throws MPTException if something goes wrong while removing the package.
	 * A specific error message will be included in the exception.
	 */
	public static void removePackage(String id) throws MPTException {
		RemoveCommand.removePackage(id);
	}

	/**
	 * Upgrades a package by the given ID.
	 * @param id the ID of the package to upgrade
	 * @return the new version of the package, or null if it is already up-to-date
	 * @throws MPTException if something goes wrong while removing or reinstalling the package.
	 * A specific error message will be included in the exception.
	 */
	public static String upgradePackage(String id) throws MPTException {
		return UpgradeCommand.upgradePackage(id);
	}

	/**
	 * Retrieves an enumeration of all installed packages.
	 * @return a list of string arrays, each representing information about a package.
	 * The first index is the package's ID, the second its "friendly" name, and the third the
	 * currently installed version.
	 * @throws MPTException if something goes wrong while indexing the installed packages
	 * A specific error message will be included in the exception.
	 */
	public static List<String[]> getInstalledPackages() throws MPTException {
		return ListPackagesCommand.getPackages();
	}

	/**
	 * Adds a repository at the given URL to the local store.
	 * @param url the URL of the repository to add
	 * @return the ID of the newly-added repository
	 * @throws MPTException if something goes wrong while connecting to or parsing the repository.
	 * A specific error message will be included in the exception.
	 */
	public static String addRepository(String url) throws MPTException {
		return AddRepositoryCommand.addRepository(url);
	}

	/**
	 * Removes a repository with the given ID from the local store.
	 * @param id the ID of the repository to remove
	 * @throws MPTException if something goes wrong while removing the repository.
	 * A specific error message will be included in the exception.
	 */
	public static void removeRepository(String id) throws MPTException {
		RemoveRepositoryCommand.removeRepository(id);
	}

	/**
	 * Fetches the package listings from all remotes and saves them to the local store.
	 * @throws MPTException if something goes wrong while connecting to or parsing the repository.
	 * A specific error message will be included in the exception.
	 */
	public static void updatePackageStore() throws MPTException {
		UpdateCommand.updateStore();
	}

	/**
	 * Retrieves an enumeration of all currently added repositories.
	 * @return a list of string arrays, each representing information about a repository.
	 * The first index is the repository's ID, and the second is its URL.
	 * @throws MPTException if something goes wrong while connecting to or parsing the repository.
	 * A specific error message will be included in the exception.
	 */
	public static List<String[]> getRepositories() throws MPTException {
		return ListRepositoriesCommand.getRepositories();
	}

}
