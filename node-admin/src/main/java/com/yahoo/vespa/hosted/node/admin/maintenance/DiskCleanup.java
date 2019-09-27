// Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.node.admin.maintenance;

import com.yahoo.collections.Pair;
import com.yahoo.vespa.hosted.node.admin.component.TaskContext;
import com.yahoo.vespa.hosted.node.admin.task.util.file.FileFinder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.yahoo.vespa.hosted.node.admin.task.util.file.FileFinder.FileAttributes;
import static com.yahoo.yolean.Exceptions.uncheck;

/**
 * @author freva
 */
public class DiskCleanup {

    private static final Logger logger = Logger.getLogger(DiskCleanup.class.getName());

    private final Clock clock;
    private final List<Rule> rules;

    public boolean cleanup(TaskContext context, long bytesToRemove) {
        if (bytesToRemove <= 0) return false;

        long[] btr = new long[] { bytesToRemove };
        List<Path> deletedPaths = new ArrayList<>();
        try {
            rules.stream()
                    .flatMap(rule -> rule.candidates().stream()
                            .map(fa -> new Pair<>(fa, rule.score(fa, clock.instant()))))
                    .sorted(Comparator.comparingDouble(Pair::getSecond))
                    .takeWhile(fa -> btr[0] > 0)
                    .forEach(pair -> {
                        if (uncheck(() -> Files.deleteIfExists(pair.getFirst().path()))) {
                            btr[0] -= pair.getFirst().size();
                            deletedPaths.add(pair.getFirst().path());
                        }
                    });

        } finally {
            if (deletedPaths.size() > 20) {
                context.log(logger, "Deleted " + deletedPaths.size() + " files because disk was getting full");
            } else if (deletedPaths.size() > 0) {
                context.log(logger, "Deleted these paths: " + deletedPaths);
            }
        }

        return !deletedPaths.isEmpty();
    }


    public static class Rule {
        private final FileFinder fileFinder;
        private final Duration maxAge;

        private Rule(Path path, int maxDepth, Duration maxAge, Predicate<FileFinder.FileAttributes> predicate) {
            this.fileFinder = FileFinder.files(path).maxDepth(maxDepth).match(predicate);
            this.maxAge = maxAge;
        }

        private Collection<FileAttributes> candidates() {
            return fileFinder.list();
        }

        private Collection<Pair<FileAttributes, Double>> scoredCandidates(Collection<FileAttributes> candidate, Instant now) {
            return candidate.stream()
                    .map(fa -> new Pair<>(fa, (double) (age.getSeconds() / maxAge.getSeconds()) * Math.log(candidate.size())))
                    .collect(Collectors.toList());
            Duration age = Duration.between(candidate.lastModifiedTime(), now);
            if (age.compareTo(maxAge) > 0) return Double.POSITIVE_INFINITY;

            return (double) (age.getSeconds() / maxAge.getSeconds()) * Math.log(candidate.size());
        }
    }

}
