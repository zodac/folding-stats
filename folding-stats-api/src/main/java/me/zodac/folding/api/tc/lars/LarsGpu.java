/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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
 * @param name              the name of the GPU
 * @param detailedName      the detailed name of the GPU
 * @param make              the make/manufacturer of the GPU
 * @param rank              the rank of the GPU compared to all other GPUs
 * @param multiplier        the multiplier applied to the GPU
 * @param ppdAverageOverall the average PPD of the GPU on all OSs
 * @see <a href="https://folding.lar.systems/api/gpu_ppd/gpu_rank_list.json">LARS GPU PPD database API</a>
 */
public record LarsGpu(@SerializedName("gpu_name") String name,
                      @SerializedName("fah_client_description") String detailedName,
                      @SerializedName("make") String make,
                      @SerializedName("gpu_rank") int rank,
                      @SerializedName("gpu_handicap") double multiplier,
                      @SerializedName("ppd_average_overall") long ppdAverageOverall) {
}
