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

package me.zodac.folding.api.tc.lars;

import com.google.gson.annotations.SerializedName;
import java.util.Collection;

/**
 * Representation of the LARS GPU PPD database API response.
 *
 * <p>
 * The response will be in the format:
 * <pre>
 *     {
 *         "api_name": "Folding@Home GPU PPD Rank List - Overall Ranks - Average All OS",
 *         "api_description": "GPUs ranked by overall PPD best to worst based on data collected and processed by https://folding.lar.systems",
 *         "api_licence": "API data is free to use to non profit folding@home teams, only condition of use is a credit / link must be provided back to https://folding.lar.systems to encourage use of the folding@home in the dark extension that makes providing this data possible.",
 *         "credit_link_website": "https://folding.lar.systems",
 *         "credit_link_chrome_extension": "https://chrome.google.com/webstore/detail/folding-at-home-in-the-da/alpjkkbjnbkddolgnicglknicbgfahoe",
 *         "date_last_update": "2022-03-22T19:44:55.4063819Z",
 *         "folding_at_home_gpu_ppd_rank_list": [
 *         ]
 *     }
 * </pre>
 *
 * @param rankedGpus the {@link Collection} of {@link LarsGpu}s included in the {@link LarsGpuResponse}
 * @see <a href="https://folding.lar.systems/api/gpu_ppd/gpu_rank_list.json">LARS GPU PPD database API</a>
 */
public record LarsGpuResponse(@SerializedName("folding_at_home_gpu_ppd_rank_list") Collection<LarsGpu> rankedGpus) {

}
