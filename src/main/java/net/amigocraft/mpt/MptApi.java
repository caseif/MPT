/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2014-2015 Maxim Roncacé <mproncace@lapis.blue>
 *
 * The MIT License (MIT)
 *
 *     Permission is hereby granted, free of charge, to any person obtaining a copy
 *     of this software and associated documentation files (the "Software"), to deal
 *     in the Software without restriction, including without limitation the rights
 *     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *     copies of the Software, and to permit persons to whom the Software is
 *     furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all
 *     copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *     SOFTWARE.
 */
package net.amigocraft.mpt;

import net.amigocraft.mpt.command.*;
import net.amigocraft.mpt.util.MPTException;

import java.util.List;

public class MptApi {

	/**
	 * Installs a package by the given ID.
	 * <strong>This method may not be called from the main thread.</strong>
	 * @param id the ID of the package to install
	 * @throws MPTException if something goes wrong while downloading or installing the package.
	 * A specific error message will be included in the exception.
	 * @since 1.0.0
	 */
	public static void installPackage(String id) throws MPTException {
		InstallCommand.installPackage(id);
	}

	/**
	 * Remove a package by the given ID.
	 * <strong>This method may not be called from the main thread.</strong>
	 * @param id the ID of the package to install
	 * @throws MPTException if something goes wrong while removing the package.
	 * A specific error message will be included in the exception.
	 * @since 1.0.0
	 */
	public static void removePackage(String id) throws MPTException {
		RemoveCommand.removePackage(id);
	}

	/**
	 * Upgrades a package by the given ID.
	 * <strong>This method may not be called from the main thread.</strong>
	 * @param id the ID of the package to upgrade
	 * @return the new version of the package, or null if it is already up-to-date
	 * @throws MPTException if something goes wrong while removing or reinstalling the package.
	 * A specific error message will be included in the exception.
	 * @since 1.0.0
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
	 * @since 1.0.0
	 */
	public static List<String[]> getInstalledPackages() throws MPTException {
		return ListInstalledCommand.getPackages();
	}

	/**
	 * Retrieves an enumeration of all packages avilable for installation.
	 * @return a list of string arrays, each representing information about a package.
	 * The first index is the package's ID, the second its "friendly" name, and the third the
	 * currently installed version.
	 * @throws MPTException if something goes wrong while indexing the installed packages
	 * A specific error message will be included in the exception.
	 * @since 1.0.0
	 */
	public static List<String[]> getAvailablePackages() throws MPTException {
		return ListAvailableCommand.getPackages();
	}

	/**
	 * Retrieves detailed information about a specific package.
	 * @param id the package to retrieve information for
	 * @return an array containing information about the package.
	 * The first array element contains its "friendly" name,
	 * the second its description,
	 * the third its latest version,
	 * the fourth its content URL,
	 * the fifth its SHA-1 checksum (null if not specified),
	 * and the sixth its currently installed version (null if not installed).
	 * @throws MPTException if something goes wrong while reading the package's information
	 * @since 1.2.0
	 */
	public static String[] getPackageInfo(String id) throws MPTException {
		return InfoCommand.getPackageInfo(id);
	}

	/**
	 * Adds a repository at the given URL to the local store.
	 * <strong>This method may not be called from the main thread.</strong>
	 * @param url the URL of the repository to add
	 * @return the ID of the newly-added repository
	 * @throws MPTException if something goes wrong while connecting to or parsing the repository.
	 * A specific error message will be included in the exception.
	 * @since 1.0.0
	 */
	public static String addRepository(String url) throws MPTException {
		return AddRepositoryCommand.addRepository(url);
	}

	/**
	 * Removes a repository with the given ID from the local store.
	 * @param id the ID of the repository to remove
	 * @throws MPTException if something goes wrong while removing the repository.
	 * A specific error message will be included in the exception.
	 * @since 1.0.0
	 */
	public static void removeRepository(String id) throws MPTException {
		RemoveRepositoryCommand.removeRepository(id);
	}

	/**
	 * Fetches the package listings from all remotes and saves them to the local store.
	 * <strong>This method may not be called from the main thread.</strong>
	 * @throws MPTException if something goes wrong while connecting to or parsing the repository.
	 * A specific error message will be included in the exception.
	 * @since 1.0.0
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
	 * @since 1.0.0
	 */
	public static List<String[]> getRepositories() throws MPTException {
		return ListRepositoriesCommand.getRepositories();
	}

}
