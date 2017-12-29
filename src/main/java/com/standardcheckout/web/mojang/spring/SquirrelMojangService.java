package com.standardcheckout.web.mojang.spring;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.cache.HashMapCache;
import com.sk89q.squirrelid.resolver.CacheForwardingService;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;
import com.sk89q.squirrelid.resolver.ParallelProfileService;
import com.sk89q.squirrelid.resolver.ProfileService;
import com.standardcheckout.web.mojang.MojangService;

@Service
public class SquirrelMojangService implements MojangService {

	private ProfileService resolver = new CacheForwardingService(HttpRepositoryService.forMinecraft(), new HashMapCache());
	private ParallelProfileService service = new ParallelProfileService(resolver, 3);

	@Override
	public UUID getUniqueIdFromName(String name) {
		if (name.length() > 16) {
			name = name.substring(0, 16);
		}
		try {
			Profile profile = service.findByName(name);
			return profile == null ? null : profile.getUniqueId();
		} catch (IOException | InterruptedException exception) {
			exception.printStackTrace(); // TODO error handling
			return null;
		}
	}

}
