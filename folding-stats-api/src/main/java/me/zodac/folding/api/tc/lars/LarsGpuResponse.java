/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.api.tc.lars;

import com.google.gson.annotations.SerializedName;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
 * @see <a href="https://folding.lar.systems/api/gpu_ppd/gpu_rank_list.json">LARS GPU PPD database API</a>
 */

@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class LarsGpuResponse {

    @SerializedName("folding_at_home_gpu_ppd_rank_list")
    private Collection<LarsGpu> rankedGpus;

    // Unused fields, kept for completion

    @SerializedName("api_name")
    private String apiName;

    @SerializedName("api_description")
    private String apiDescription;

    @SerializedName("api_licence")
    private String apiLicence;

    @SerializedName("credit_link_website")
    private String creditLinkWebsite;

    @SerializedName("credit_link_chrome_extension")
    private String creditLinkChromeExtension;

    @SerializedName("date_last_update")
    private String dateOfLastUpdate;

    /**
     * Default constructor.
     */
    public LarsGpuResponse() {

    }
}