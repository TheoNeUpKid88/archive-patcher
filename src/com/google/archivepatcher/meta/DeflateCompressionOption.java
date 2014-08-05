// Copyright 2014 Google Inc. All rights reserved.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.archivepatcher.meta;

/**
 * An enumeration of all the deflate compression options listed in the ZIP
 * "appnote" specification at https://www.pkware.com/support/zip-app-note.
 * See the appnote documentation for details on algorithms and meanings of
 * the individual values.
 */
@SuppressWarnings("javadoc")
public enum DeflateCompressionOption {
    NORMAL,
    MAXIMUM,
    FAST,
    SUPERFAST;
}
