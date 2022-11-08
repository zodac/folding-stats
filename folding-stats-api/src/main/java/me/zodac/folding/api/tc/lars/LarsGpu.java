/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Representation of an individual GPU from the LARS DB.
 *
 * <p>
 * Each GPU will be in the format:
 * <pre>
 *     {
 *         "gpu_rank": 1,
 *         "gpu_handicap": 1,
 *         "gpu_name": "GeForce RTX 3080 Ti",
 *         "sub_version": "",
 *         "gpu_chip": "GA102",
 *         "make": "Nvidia",
 *         "fah_client_description": "GA102 [GeForce RTX 3080 Ti]",
 *         "ppd_average_overall": 7323554,
 *         "ppd_samples_overall": 45721,
 *         "ppd_average_linux": 8745636,
 *         "ppd_samples_linux": 1314,
 *         "ppd_average_windows": 6931872,
 *         "ppd_samples_windows": 44407,
 *         "url_profile": "https://folding.lar.systems/gpu_ppd/brands/nvidia/folding_profile/ga102_geforce_rtx_3080_ti"
 *     }
 * </pre>
 *
 * @see <a href="https://folding.lar.systems/api/gpu_ppd/gpu_rank_list.json">LARS GPU PPD database API</a>
 */
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = false) // Need #get*()
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class LarsGpu {

    @SerializedName("gpu_name")
    private String name;

    @SerializedName("fah_client_description")
    private String detailedName;

    @SerializedName("make")
    private String make;

    @SerializedName("gpu_rank")
    private int rank;

    @SerializedName("gpu_handicap")
    private double multiplier;

    @SerializedName("ppd_average_overall")
    private long ppdAverageOverall;

    // Unused fields, kept for completion

    @SerializedName("sub_version")
    private String subVersion;

    @SerializedName("gpu_chip")
    private String chip;

    @SerializedName("ppd_samples_overall")
    private long ppdSamplesOverall;

    @SerializedName("ppd_average_linux")
    private long ppdAverageLinux;

    @SerializedName("ppd_samples_linux")
    private long ppdSamplesLinux;

    @SerializedName("ppd_average_windows")
    private long ppdAverageWindows;

    @SerializedName("ppd_samples_windows")
    private long ppdSamplesWindows;

    @SerializedName("url_profile")
    private String urlProfile;
}