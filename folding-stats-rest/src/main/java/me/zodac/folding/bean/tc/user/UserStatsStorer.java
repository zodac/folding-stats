/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.bean.tc.user;

import java.time.LocalDateTime;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.tc.LeaderboardStatsGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Stores the stats results for the {@code Team Competition}. Takes the current stats and stores them with the current
 * {@link java.time.ZoneOffset#UTC} {@link LocalDateTime}.
 */
@Component
public class UserStatsStorer {

    private static final Logger LOGGER = LogManager.getLogger();

    private final LeaderboardStatsGenerator leaderboardStatsGenerator;
    private final StatsRepository statsRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param leaderboardStatsGenerator the {@link LeaderboardStatsGenerator}
     * @param statsRepository           the {@link StatsRepository}
     */
    @Autowired
    public UserStatsStorer(final LeaderboardStatsGenerator leaderboardStatsGenerator, final StatsRepository statsRepository) {
        this.leaderboardStatsGenerator = leaderboardStatsGenerator;
        this.statsRepository = statsRepository;
    }

    /**
     * Stores the {@link MonthlyResult} for the current {@link java.time.ZoneOffset#UTC} date-time.
     *
     * <p>
     * The {@link MonthlyResult} will be generated using {@link LeaderboardStatsGenerator#generateTeamLeaderboards()} and
     * {@link LeaderboardStatsGenerator#generateUserCategoryLeaderboards()}.
     *
     * <p>
     * The JSON output will be in the form:
     * <pre>
     * {
     *   "teamLeaderboard": [
     *     {
     *       "team": {
     *         "id": 1,
     *         "teamName": "Team 1",
     *         "forumLink": "https://google.com"
     *       },
     *       "teamPoints": 18696798,
     *       "teamMultipliedPoints": 158584992,
     *       "teamUnits": 68,
     *       "rank": 1,
     *       "diffToLeader": 0,
     *       "diffToNext": 0
     *     },
     *     {
     *       "team": {
     *         "id": 2,
     *         "teamName": "Team 2",
     *         "forumLink": "https://google.com"
     *       },
     *       "teamPoints": 10475450,
     *       "teamMultipliedPoints": 122304697,
     *       "teamUnits": 57,
     *       "rank": 2,
     *       "diffToLeader": 36280295,
     *       "diffToNext": 36280295
     *     },
     *     {
     *       "team": {
     *         "id": 3,
     *         "teamName": "Team 3",
     *         "forumLink": "https://google.com"
     *       },
     *       "teamPoints": 26326940,
     *       "teamMultipliedPoints": 121282206,
     *       "teamUnits": 187,
     *       "rank": 3,
     *       "diffToLeader": 37302786,
     *       "diffToNext": 1022491
     *     },
     *     {
     *       "team": {
     *         "id": 4,
     *         "teamName": "Team 4",
     *         "forumLink": "https://google.com"
     *       },
     *       "teamPoints": 21333939,
     *       "teamMultipliedPoints": 112172040,
     *       "teamUnits": 76,
     *       "rank": 4,
     *       "diffToLeader": 46412952,
     *       "diffToNext": 9110166
     *     },
     *     {
     *       "team": {
     *         "id": 5,
     *         "teamName": "Team 5",
     *         "forumLink": "https://google.com"
     *       },
     *       "teamPoints": 3626848,
     *       "teamMultipliedPoints": 111649093,
     *       "teamUnits": 28,
     *       "rank": 5,
     *       "diffToLeader": 46935899,
     *       "diffToNext": 522947
     *     }
     *   ],
     *   "userCategoryLeaderboard": {
     *     "WILDCARD": [
     *       {
     *         "user": {
     *           "id": 1,
     *           "foldingUserName": "User1",
     *           "displayName": "User 1",
     *           "passkey": "4f5535e1************************",
     *           "category": "WILDCARD",
     *           "profileLink": "https://google.com",
     *           "liveStatsLink": "https://google.com",
     *           "hardware": {
     *             "id": 1,
     *             "hardwareName": "Hardware1",
     *             "displayName": "Hardware 1",
     *             "hardwareMake": "NVIDIA",
     *             "hardwareType": "GPU",
     *             "multiplier": 42.75,
     *             "averagePpd": 154070
     *           },
     *           "team": {
     *             "id": 1,
     *             "teamName": "Team 1",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 1495839,
     *         "multipliedPoints": 63947117,
     *         "units": 11,
     *         "rank": 1,
     *         "diffToLeader": 0,
     *         "diffToNext": 0
     *       },
     *       {
     *         "user": {
     *           "id": 2,
     *           "foldingUserName": "User2",
     *           "displayName": "User 2",
     *           "passkey": "b1e51fa2************************",
     *           "category": "WILDCARD",
     *           "profileLink": "https://google.com",
     *           "hardware": {
     *             "id": 2,
     *             "hardwareName": "Hardware2",
     *             "displayName": "Hardware 2",
     *             "hardwareMake": "AMD",
     *             "hardwareType": "GPU",
     *             "multiplier": 98.72,
     *             "averagePpd": 66718
     *           },
     *           "team": {
     *             "id": 2,
     *             "teamName": "Team 2",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 487199,
     *         "multipliedPoints": 48096285,
     *         "units": 4,
     *         "rank": 2,
     *         "diffToLeader": 15850832,
     *         "diffToNext": 15850832
     *       },
     *       {
     *         "user": {
     *           "id": 3,
     *           "foldingUserName": "User3",
     *           "displayName": "User 3",
     *           "passkey": "f2ac7962************************",
     *           "category": "WILDCARD",
     *           "profileLink": "https://google.com",
     *           "hardware": {
     *             "id": 3,
     *             "hardwareName": "Hardware3",
     *             "displayName": "Hardware 3",
     *             "hardwareMake": "NVIDIA",
     *             "hardwareType": "GPU",
     *             "multiplier": 38.6,
     *             "averagePpd": 170655
     *           },
     *           "team": {
     *             "id": 3,
     *             "teamName": "Team 3",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 1859293,
     *         "multipliedPoints": 45808851,
     *         "units": 11,
     *         "rank": 3,
     *         "diffToLeader": 18138266,
     *         "diffToNext": 2287434
     *       },
     *       {
     *         "user": {
     *           "id": 4,
     *           "foldingUserName": "User4",
     *           "displayName": "User 4",
     *           "passkey": "0c710b59************************",
     *           "category": "WILDCARD",
     *           "profileLink": "https://google.com",
     *           "hardware": {
     *             "id": 4,
     *             "hardwareName": "Hardware4",
     *             "displayName": "Hardware 4",
     *             "hardwareMake": "NVIDIA",
     *             "hardwareType": "GPU",
     *             "multiplier": 201.99,
     *             "averagePpd": 32608
     *           },
     *           "team": {
     *             "id": 5,
     *             "teamName": "Team 5",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 142885,
     *         "multipliedPoints": 28861341,
     *         "units": 3,
     *         "rank": 4,
     *         "diffToLeader": 35085776,
     *         "diffToNext": 16947510
     *       },
     *       {
     *         "user": {
     *           "id": 5,
     *           "foldingUserName": "User5",
     *           "displayName": "User 5",
     *           "passkey": "52e74b68************************",
     *           "category": "WILDCARD",
     *           "profileLink": "https://google.com",
     *           "hardware": {
     *             "id": 5,
     *             "hardwareName": "Hardware5",
     *             "displayName": "Hardware 5",
     *             "hardwareMake": "NVIDIA",
     *             "hardwareType": "GPU",
     *             "multiplier": 11.25,
     *             "averagePpd": 585655
     *           },
     *           "team": {
     *             "id": 4,
     *             "teamName": "Team 4",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 2142522,
     *         "multipliedPoints": 24103373,
     *         "units": 14,
     *         "rank": 5,
     *         "diffToLeader": 39843744,
     *         "diffToNext": 4757968
     *       }
     *     ],
     *     "AMD_GPU": [
     *       {
     *         "user": {
     *           "id": 6,
     *           "foldingUserName": "User6",
     *           "displayName": "User 6",
     *           "passkey": "13d79997************************",
     *           "category": "AMD_GPU",
     *           "profileLink": "https://google.com",
     *           "liveStatsLink": "https://google.com",
     *           "hardware": {
     *             "id": 6,
     *             "hardwareName": "Hardware6",
     *             "displayName": "Hardware 6",
     *             "hardwareMake": "AMD",
     *             "hardwareType": "GPU",
     *             "multiplier": 14.14,
     *             "averagePpd": 465865
     *           },
     *           "team": {
     *             "id": 1,
     *             "teamName": "Team 1",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 3063596,
     *         "multipliedPoints": 43319247,
     *         "units": 16,
     *         "rank": 1,
     *         "diffToLeader": 0,
     *         "diffToNext": 0
     *       },
     *       {
     *         "user": {
     *           "id": 7,
     *           "foldingUserName": "User7",
     *           "displayName": "User 7",
     *           "passkey": "5cb32aa3************************",
     *           "category": "AMD_GPU",
     *           "profileLink": "https://google.com",
     *           "hardware": {
     *             "id": 6,
     *             "hardwareName": "Hardware6",
     *             "displayName": "Hardware 6",
     *             "hardwareMake": "AMD",
     *             "hardwareType": "GPU",
     *             "multiplier": 14.14,
     *             "averagePpd": 465865
     *           },
     *           "team": {
     *             "id": 4,
     *             "teamName": "Team 4",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 2984700,
     *         "multipliedPoints": 42203658,
     *         "units": 17,
     *         "rank": 2,
     *         "diffToLeader": 1115589,
     *         "diffToNext": 1115589
     *       },
     *       {
     *         "user": {
     *           "id": 8,
     *           "foldingUserName": "User8",
     *           "displayName": "User 8",
     *           "passkey": "4b1e4dac************************",
     *           "category": "AMD_GPU",
     *           "profileLink": "https://google.com",
     *           "hardware": {
     *             "id": 6,
     *             "hardwareName": "Hardware6",
     *             "displayName": "Hardware 6",
     *             "hardwareMake": "AMD",
     *             "hardwareType": "GPU",
     *             "multiplier": 14.14,
     *             "averagePpd": 465865
     *           },
     *           "team": {
     *             "id": 2,
     *             "teamName": "Team 2",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 2873022,
     *         "multipliedPoints": 40624531,
     *         "units": 15,
     *         "rank": 3,
     *         "diffToLeader": 2694716,
     *         "diffToNext": 1579127
     *       },
     *       {
     *         "user": {
     *           "id": 9,
     *           "foldingUserName": "User9",
     *           "displayName": "User 9",
     *           "passkey": "45fc2116************************",
     *           "category": "AMD_GPU",
     *           "profileLink": "https://google.com",
     *           "hardware": {
     *             "id": 6,
     *             "hardwareName": "Hardware6",
     *             "displayName": "Hardware 6",
     *             "hardwareMake": "AMD",
     *             "hardwareType": "GPU",
     *             "multiplier": 14.14,
     *             "averagePpd": 465865
     *           },
     *           "team": {
     *             "id": 5,
     *             "teamName": "Team 5",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 2500551,
     *         "multipliedPoints": 35357791,
     *         "units": 14,
     *         "rank": 4,
     *         "diffToLeader": 7961456,
     *         "diffToNext": 5266740
     *       },
     *       {
     *         "user": {
     *           "id": 10,
     *           "foldingUserName": "User10",
     *           "displayName": "User 10",
     *           "passkey": "88a9cdf2************************",
     *           "category": "AMD_GPU",
     *           "hardware": {
     *             "id": 7,
     *             "hardwareName": "Hardware7",
     *             "displayName": "Hardware 7",
     *             "hardwareMake": "AMD",
     *             "hardwareType": "GPU",
     *             "multiplier": 3.28,
     *             "averagePpd": 2008233
     *           },
     *           "team": {
     *             "id": 3,
     *             "teamName": "Team 3",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 8532556,
     *         "multipliedPoints": 27986784,
     *         "units": 31,
     *         "rank": 5,
     *         "diffToLeader": 15332463,
     *         "diffToNext": 7371007
     *       }
     *     ],
     *     "NVIDIA_GPU": [
     *       {
     *         "user": {
     *           "id": 11,
     *           "foldingUserName": "User11",
     *           "displayName": "User 11",
     *           "passkey": "8b98b0a2************************",
     *           "category": "NVIDIA_GPU",
     *           "profileLink": "https://google.com",
     *           "liveStatsLink": "https://google.com",
     *           "hardware": {
     *             "id": 8,
     *             "hardwareName": "Hardware8",
     *             "displayName": "Hardware 8",
     *             "hardwareMake": "NVIDIA",
     *             "hardwareType": "GPU",
     *             "multiplier": 3.63,
     *             "averagePpd": 1816142
     *           },
     *           "team": {
     *             "id": 1,
     *             "teamName": "Team 1",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 14137363,
     *         "multipliedPoints": 51318628,
     *         "units": 41,
     *         "rank": 1,
     *         "diffToLeader": 0,
     *         "diffToNext": 0
     *       },
     *       {
     *         "user": {
     *           "id": 12,
     *           "foldingUserName": "User12",
     *           "displayName": "User 12",
     *           "passkey": "366cb50f************************",
     *           "category": "NVIDIA_GPU",
     *           "hardware": {
     *             "id": 9,
     *             "hardwareName": "Hardware9",
     *             "displayName": "Hardware 9",
     *             "hardwareMake": "NVIDIA",
     *             "hardwareType": "GPU",
     *             "multiplier": 2.98,
     *             "averagePpd": 2213983
     *           },
     *           "team": {
     *             "id": 3,
     *             "teamName": "Team 3",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "MEMBER"
     *         },
     *         "points": 15935091,
     *         "multipliedPoints": 47486571,
     *         "units": 145,
     *         "rank": 2,
     *         "diffToLeader": 3832057,
     *         "diffToNext": 3832057
     *       },
     *       {
     *         "user": {
     *           "id": 13,
     *           "foldingUserName": "User13",
     *           "displayName": "User 13",
     *           "passkey": "c031f83e************************",
     *           "category": "NVIDIA_GPU",
     *           "profileLink": "https://google.com",
     *           "liveStatsLink": "https://google.com",
     *           "hardware": {
     *             "id": 10,
     *             "hardwareName": "Hardware10",
     *             "displayName": "Hardware 10",
     *             "hardwareMake": "NVIDIA",
     *             "hardwareType": "GPU",
     *             "multiplier": 48.23,
     *             "averagePpd": 136571
     *           },
     *           "team": {
     *             "id": 5,
     *             "teamName": "Team 5",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "CAPTAIN"
     *         },
     *         "points": 983412,
     *         "multipliedPoints": 47429961,
     *         "units": 11,
     *         "rank": 3,
     *         "diffToLeader": 3888667,
     *         "diffToNext": 56610
     *       },
     *       {
     *         "user": {
     *           "id": 14,
     *           "foldingUserName": "User14",
     *           "displayName": "User 14",
     *           "passkey": "5fe45546************************",
     *           "category": "NVIDIA_GPU",
     *           "profileLink": "https://google.com",
     *           "hardware": {
     *             "id": 11,
     *             "hardwareName": "Hardware11",
     *             "displayName": "Hardware 11",
     *             "hardwareMake": "NVIDIA",
     *             "hardwareType": "GPU",
     *             "multiplier": 2.83,
     *             "averagePpd": 2326189
     *           },
     *           "team": {
     *             "id": 4,
     *             "teamName": "Team 4",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "CAPTAIN"
     *         },
     *         "points": 16206717,
     *         "multipliedPoints": 45865009,
     *         "units": 45,
     *         "rank": 4,
     *         "diffToLeader": 5453619,
     *         "diffToNext": 1564952
     *       },
     *       {
     *         "user": {
     *           "id": 15,
     *           "foldingUserName": "User15",
     *           "displayName": "User 15",
     *           "passkey": "82edd687************************",
     *           "category": "NVIDIA_GPU",
     *           "profileLink": "https://google.com",
     *           "hardware": {
     *             "id": 12,
     *             "hardwareName": "Hardware12",
     *             "displayName": "Hardware 12",
     *             "hardwareMake": "NVIDIA",
     *             "hardwareType": "GPU",
     *             "multiplier": 4.72,
     *             "averagePpd": 1395095
     *           },
     *           "team": {
     *             "id": 2,
     *             "teamName": "Team 2",
     *             "forumLink": "https://google.com"
     *           },
     *           "role": "CAPTAIN"
     *         },
     *         "points": 7115229,
     *         "multipliedPoints": 33583881,
     *         "units": 38,
     *         "rank": 5,
     *         "diffToLeader": 17734747,
     *         "diffToNext": 12281128
     *       }
     *     ]
     *   },
     *   "utcTimestamp": {
     *     "date": {
     *       "year": 2021,
     *       "month": 11,
     *       "day": 8
     *     },
     *     "time": {
     *       "hour": 23,
     *       "minute": 8,
     *       "second": 35,
     *       "nano": 596553000
     *     }
     *   }
     * }
     * </pre>
     */
    public void storeMonthlyResult() {
        final MonthlyResult monthlyResult = MonthlyResult.createWithCurrentDateTime(
            leaderboardStatsGenerator.generateTeamLeaderboards(),
            leaderboardStatsGenerator.generateUserCategoryLeaderboards()
        );

        if (monthlyResult.hasNoStats()) {
            LOGGER.error("Not storing result, result has no stats: {}", monthlyResult);
            return;
        }

        final MonthlyResult createdMonthlyResult = statsRepository.createMonthlyResult(monthlyResult);
        LOGGER.info("Storing TC results for {}", createdMonthlyResult.utcTimestamp());
    }
}
